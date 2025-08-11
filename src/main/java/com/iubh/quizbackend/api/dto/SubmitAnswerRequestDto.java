package com.iubh.quizbackend.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class SubmitAnswerRequestDto {
    @NotNull
    private UUID questionId;

    @NotNull
    private UUID selectedAnswerId;
}
