package com.iubh.quizbackend.service;

import com.iubh.quizbackend.api.dto.LobbyEventDto;
import com.iubh.quizbackend.api.dto.NotificationDto;

import com.iubh.quizbackend.api.dto.quiz.QuizLobbyDto;
import com.iubh.quizbackend.entity.module.Module;
import com.iubh.quizbackend.entity.quiz.QuizLobby;
import com.iubh.quizbackend.entity.quiz.QuizLobbyStatus;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.mapper.QuizLobbyMapper;
import com.iubh.quizbackend.repository.ModuleRepository;
import com.iubh.quizbackend.repository.QuizLobbyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizLobbyService {

    private final QuizLobbyRepository quizLobbyRepository;
    private final ModuleRepository moduleRepository;
    private final QuizLobbyMapper quizLobbyMapper;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String LOBBY_TOPIC = "/topic/lobbies";
    private static final String SINGLE_LOBBY_TOPIC_PREFIX = "/topic/lobby/";



    /**
     * Creates a new quiz lobby and notifies all clients.
     */
    @Transactional
    public QuizLobby createLobby(User host, UUID moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Module not found with id: " + moduleId));

        QuizLobby lobby = QuizLobby.builder()
                .host(host)
                .module(module)
                .status(QuizLobbyStatus.WAITING)
                .build();

        lobby.getParticipants().add(host); // The host is also a participant

        QuizLobby savedLobby = quizLobbyRepository.save(lobby);
        broadcastLobbiesUpdate();
        return savedLobby;
    }

    /**
     * Allows a user to join an existing quiz lobby.
     */
    @Transactional
    public QuizLobby joinLobby(User user, UUID lobbyId) {
        QuizLobby lobby = quizLobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new EntityNotFoundException("Lobby not found with id: " + lobbyId));

        if (lobby.getStatus() != QuizLobbyStatus.WAITING) {
            throw new IllegalStateException("Lobby is not in a waiting state.");
        }

        // --- THIS IS THE FIX ---
        // 1. Add the new user to the participants set.
        lobby.getParticipants().add(user);

        // 2. Save the updated lobby to the database.
        QuizLobby updatedLobby = quizLobbyRepository.save(lobby);

        // 3. Broadcast the fully updated lobby state to all clients.
        broadcastSingleLobbyUpdate(updatedLobby, LobbyEventDto.EventType.LOBBY_JOINED);
        //   broadcastLobbyEvent(new LobbyEventDto(LobbyEventDto.EventType.LOBBY_JOINED, quizLobbyMapper.toDto(updatedLobby)));
        broadcastLobbiesUpdate();
        return updatedLobby;
    }


    @Transactional
    public QuizLobby leaveLobby(User user, UUID lobbyId) {
        QuizLobby lobby = quizLobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new EntityNotFoundException("Lobby not found with id: " + lobbyId));

        // Nur im WAITING-Status erlaubt
        if (lobby.getStatus() != QuizLobbyStatus.WAITING) {
            throw new IllegalStateException("Cannot leave a lobby that is not in WAITING state.");
        }

        // War der Nutzer überhaupt Teilnehmer?
        boolean wasParticipant = lobby.getParticipants().removeIf(p -> p.getId().equals(user.getId()));
        if (!wasParticipant) {
            // idempotent: entweder Exception werfen oder leise zurückgeben
            return lobby;
        }

        // Fall A: Host verlässt die Lobby
        if (lobby.getHost().getId().equals(user.getId())) {
            if (lobby.getParticipants().isEmpty()) {
                // Niemand mehr übrig → Lobby canceln
                lobby.setStatus(QuizLobbyStatus.CANCELLED);
                QuizLobby cancelled = quizLobbyRepository.save(lobby);

                // 1) Einzel-Topic: Cancel-Event
                broadcastSingleLobbyUpdate(cancelled, LobbyEventDto.EventType.LOBBY_CANCELLED);
                // 2) Liste aktualisieren
                broadcastLobbiesUpdate();

                return cancelled;
            } else {
                // Nächsten Teilnehmer zum Host machen (einfach 1. in der Menge)
                User newHost = lobby.getParticipants().iterator().next();
                lobby.setHost(newHost);
            }
        }

        // Fall B: normaler Teilnehmer verlässt die Lobby (oder Host wurde bereits ersetzt)
        QuizLobby updated = quizLobbyRepository.save(lobby);

        // 1) Einzel-Topic: LOBBY_LEFT (mit kompletter aktualisierter Lobby)
        broadcastSingleLobbyUpdate(updated, LobbyEventDto.EventType.LOBBY_LEFT);
        // 2) Liste aktualisieren
        broadcastLobbiesUpdate();

        return updated;
    }


    /**
     * Cancels a lobby, typically by the host.
     */
    @Transactional
    public void cancelLobby(User user, UUID lobbyId) {
        QuizLobby lobby = quizLobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new EntityNotFoundException("Lobby not found with id: " + lobbyId));

        if (!lobby.getHost().getId().equals(user.getId())) {
            throw new SecurityException("Only the host can cancel the lobby.");
        }

        lobby.setStatus(QuizLobbyStatus.CANCELLED);
        quizLobbyRepository.save(lobby);
        broadcastLobbiesUpdate();
        broadcastSingleLobbyUpdate(lobby, LobbyEventDto.EventType.LOBBY_CANCELLED);
        //  broadcastLobbyEvent(new LobbyEventDto(LobbyEventDto.EventType.LOBBY_CANCELLED, lobbyId));
    }


    /**
     * Retrieves all lobbies that are currently in the WAITING state.
     */
    @Transactional(readOnly = true)
    public List<QuizLobbyDto> getWaitingLobbies() {
        return quizLobbyRepository.findByStatus(QuizLobbyStatus.WAITING)
                .stream()
                .map(quizLobbyMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Broadcasts the current list of waiting lobbies to all subscribed clients.
     */
    public void broadcastLobbiesUpdate() {
        List<QuizLobbyDto> waitingLobbies = getWaitingLobbies();

        // --- CREATE A SPECIFIC NOTIFICATION DTO ---
        NotificationDto<List<QuizLobbyDto>> notification = new NotificationDto<>(
                NotificationDto.NotificationType.LOBBY_UPDATE,
                waitingLobbies
        );

        messagingTemplate.convertAndSend(LOBBY_TOPIC, notification);
    }

    @Transactional(readOnly = true)
    public QuizLobby getLobbyById(UUID lobbyId) {
        return quizLobbyRepository.findById(lobbyId)
                .orElseThrow(() -> new EntityNotFoundException("Lobby not found with id: " + lobbyId));
    }


    /**
     * Broadcasts an update for a single, specific lobby to its dedicated topic.
     *
     * @param lobby The lobby that has been updated.
     */
    public void broadcastSingleLobbyUpdate(QuizLobby lobby, LobbyEventDto.EventType eventType) {
        QuizLobbyDto lobbyDto = quizLobbyMapper.toDto(lobby);
        String destination = SINGLE_LOBBY_TOPIC_PREFIX + lobby.getId();

        LobbyEventDto lobbyEventDto = new LobbyEventDto(eventType, lobbyDto);
        // No need to wrap this in a NotificationDto, as the topic is already specific
        messagingTemplate.convertAndSend(destination, lobbyEventDto);
    }


//    private void broadcastLobbyEvent(LobbyEventDto event) {
//        messagingTemplate.convertAndSend(LOBBY_TOPIC, event);
//    }


}