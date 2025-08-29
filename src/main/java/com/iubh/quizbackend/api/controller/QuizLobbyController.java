package com.iubh.quizbackend.api.controller;

import com.iubh.quizbackend.api.dto.quiz.QuizLobbyDto;
import com.iubh.quizbackend.entity.quiz.QuizLobby;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.mapper.QuizLobbyMapper;
import com.iubh.quizbackend.service.QuizLobbyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/v1/lobbies")
@RequiredArgsConstructor
public class QuizLobbyController {

    private final QuizLobbyService quizLobbyService;
    private final QuizLobbyMapper quizLobbyMapper;

    @PostMapping("/create/{moduleId}")
    public ResponseEntity<QuizLobbyDto> createLobby(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID moduleId
    ) {
        QuizLobby lobby = quizLobbyService.createLobby(currentUser, moduleId);
        return ResponseEntity.ok(quizLobbyMapper.toDto(lobby));
    }

    @PostMapping("/join/{lobbyId}")
    public ResponseEntity<QuizLobbyDto> joinLobby(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID lobbyId
    ) {
        QuizLobby lobby = quizLobbyService.joinLobby(currentUser, lobbyId);
        return ResponseEntity.ok(quizLobbyMapper.toDto(lobby));
    }

    @PostMapping("/cancel/{lobbyId}")
    public ResponseEntity<Void> cancelLobby(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID lobbyId
    ) {
        quizLobbyService.cancelLobby(currentUser, lobbyId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/waiting")
    public ResponseEntity<List<QuizLobbyDto>> getWaitingLobbies() {
        return ResponseEntity.ok(quizLobbyService.getWaitingLobbies());
    }

    /**
     * This WebSocket endpoint is used by clients to request the latest list of lobbies.
     * When a message is sent to /app/lobbies/get, this method will be invoked,
     * and the return value will be broadcast to all subscribers of /topic/lobbies.
     */
    @MessageMapping("/lobbies/get")
    @SendTo("/topic/lobbies")
    public List<QuizLobbyDto> getLobbies() {
        return quizLobbyService.getWaitingLobbies();
    }


    @DeleteMapping("/{lobbyId}/participants/me")
    public ResponseEntity<Void> leaveLobbyDelete(
            @PathVariable UUID lobbyId,
            @AuthenticationPrincipal User currentUser) {

        quizLobbyService.leaveLobby(currentUser, lobbyId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{lobbyId}")
    public ResponseEntity<QuizLobbyDto> getLobbyById(@PathVariable UUID lobbyId) {
        QuizLobby lobby = quizLobbyService.getLobbyById(lobbyId);
        return ResponseEntity.ok(quizLobbyMapper.toDto(lobby));
    }
}