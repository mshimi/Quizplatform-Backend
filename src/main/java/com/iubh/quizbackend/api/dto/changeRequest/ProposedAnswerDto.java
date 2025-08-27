package com.iubh.quizbackend.api.dto.changeRequest;

import lombok.Data;
import java.util.UUID;

@Data
public class ProposedAnswerDto {
    private UUID id;
    private String text;
    private Boolean isCorrect;
}