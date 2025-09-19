package com.iubh.quizbackend.api.dto.question;


import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class QuestionSummaryDto {
    private UUID id;
    private String questionText;
}
