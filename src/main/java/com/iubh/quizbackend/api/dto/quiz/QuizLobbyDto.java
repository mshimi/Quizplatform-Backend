package com.iubh.quizbackend.api.dto.quiz;

import com.iubh.quizbackend.api.dto.ModuleSummaryDto;
import com.iubh.quizbackend.api.dto.UserDto;
import com.iubh.quizbackend.entity.quiz.QuizLobbyStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
public class QuizLobbyDto {
    private UUID id;
    private UserDto host;
    private ModuleSummaryDto module;
    private Set<UserDto> participants;
    private QuizLobbyStatus status;
    private LocalDateTime createdAt;
}