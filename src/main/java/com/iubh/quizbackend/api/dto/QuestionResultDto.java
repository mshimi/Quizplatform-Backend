package com.iubh.quizbackend.api.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class QuestionResultDto {
    private UUID id;
    private String questionText;
    private boolean wasAnsweredCorrectly; // Was the user's answer for this question correct?
    private List<AnswerResultDto> answers;
}