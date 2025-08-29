package com.iubh.quizbackend.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iubh.quizbackend.api.dto.quiz.QuizLobbyDto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Don't serialize null fields
public class LobbyEventDto {

    private EventType type;
    private QuizLobbyDto lobby;
    private UUID lobbyId; // Used for cancellations

    public enum EventType {
        LOBBY_CREATED,
        LOBBY_CANCELLED,
        LOBBY_JOINED,
        LOBBY_LEFT,
        LOBBY_UPDATE
    }

    // Constructor for created, joined, left, update events
    public LobbyEventDto(EventType type, QuizLobbyDto lobby) {
        this.type = type;
        this.lobby = lobby;
    }

    // Constructor for cancellation events
    public LobbyEventDto(EventType type, UUID lobbyId) {
        this.type = type;
        this.lobbyId = lobbyId;
    }
}