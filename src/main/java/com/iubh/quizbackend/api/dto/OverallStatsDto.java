package com.iubh.quizbackend.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OverallStatsDto {
    private int averageScore;
    private int quizzesCompleted;
    private int totalQuestionsAnswered;
    private int correctAnswerRatio;
}
