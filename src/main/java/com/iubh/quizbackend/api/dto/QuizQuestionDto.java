package com.iubh.quizbackend.api.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

// Eine Frage, wie sie während des Quiz angezeigt wird (mit sortierten Antworten)
@Data
public class QuizQuestionDto {
    private UUID id;
    private String questionText;
    private List<QuizAnswerDto> answers; // Die Antworten sind bereits in der richtigen, zufälligen Reihenfolge
}