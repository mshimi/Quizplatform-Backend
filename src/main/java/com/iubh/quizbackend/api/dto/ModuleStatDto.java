package com.iubh.quizbackend.api.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class ModuleStatDto {
    private UUID moduleId;
    private String moduleTitle;
    private int quizzesPlayed;
    private int averageScore;
    private int correctAnswers;
    private int totalAnswers;
}
