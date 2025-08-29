
// src/main/java/com/iubh/quizbackend/api/dto/live/SubmitLiveAnswerDto.java
package com.iubh.quizbackend.api.dto.live;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SubmitLiveAnswerDto(
        @Min(0) int questionIndex,
        @NotNull UUID answerId
) {}
