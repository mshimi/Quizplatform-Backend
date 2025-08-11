package com.iubh.quizbackend.api.dto;

import com.iubh.quizbackend.entity.quiz.QuizStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class QuizResultDto {
    private UUID id;
    private ModuleSummaryDto module;
    private QuizStatus status;
    private LocalDateTime completedAt;
    private int numberOfQuestions;
    private long numberOfCorrectAnswers;
    private int scorePercentage;
    private List<QuestionResultDto> questionResults;
}