package com.iubh.quizbackend.api.dto.question;


import lombok.Data;

import java.util.UUID;

@Data
public class QuestionSummaryDto {
    private UUID id;
    private String questionText;
}
