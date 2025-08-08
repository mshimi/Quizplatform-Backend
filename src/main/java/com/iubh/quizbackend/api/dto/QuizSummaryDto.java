package com.iubh.quizbackend.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iubh.quizbackend.entity.quiz.QuizStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A lightweight summary of a user's quiz attempt, suitable for lists.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuizSummaryDto {
    private UUID id;
    private QuizStatus status;
    private int numberOfQuestions;
    private long numberOfCorrectAnswers;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    private ModuleSummaryDto module;
}