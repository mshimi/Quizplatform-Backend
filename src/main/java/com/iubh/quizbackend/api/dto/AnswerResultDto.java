package com.iubh.quizbackend.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AnswerResultDto {
    private UUID id;
    private String text;
    private boolean isCorrect; // The actual correct status of the answer
    private boolean isSelected; // Whether the user selected this answer
}