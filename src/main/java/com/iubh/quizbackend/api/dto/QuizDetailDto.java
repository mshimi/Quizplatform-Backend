package com.iubh.quizbackend.api.dto;

import com.iubh.quizbackend.entity.quiz.QuizStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Data
public class QuizDetailDto {
    private UUID id;
    private ModuleSummaryDto module;
    private QuizStatus status;
    private LocalDateTime createdAt;
    private Set<QuizQuestionDto> questions;

    private Map<UUID, UUID> selectedAnswers;
}