package com.iubh.quizbackend.service;

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

        lobby.getParticipants().add(user);
        QuizLobby updatedLobby = quizLobbyRepository.save(lobby);
        broadcastLobbiesUpdate();
        return updatedLobby;
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



}