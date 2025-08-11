package com.iubh.quizbackend.api.dto;

import lombok.Data;
import java.util.UUID;

// Eine Antwort, wie sie während des Quiz angezeigt wird (ohne isCorrect)
@Data
public class QuizAnswerDto {
    private UUID id;
    private String text;
}
