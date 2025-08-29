// src/main/java/com/iubh/quizbackend/api/dto/live/StartLiveSessionResponseDto.java
package com.iubh.quizbackend.api.dto.live;

import java.time.Instant;
import java.util.UUID;

public record StartLiveSessionResponseDto(
        UUID sessionId,
        Instant startAt,
        int totalQuestions,
        int questionDurationSec,
        int bufferDurationSec
) {}
