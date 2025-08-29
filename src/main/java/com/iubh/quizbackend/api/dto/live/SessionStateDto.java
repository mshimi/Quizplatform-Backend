// src/main/java/com/iubh/quizbackend/api/dto/live/SessionStateDto.java
package com.iubh.quizbackend.api.dto.live;

import com.iubh.quizbackend.entity.quiz.SessionStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SessionStateDto(
        SessionStatus status,
        int currentIndex,
        int totalQuestions,
        Instant startAt,
        Instant endsAt,
        QuestionDto question,   // null unless RUNNING
        YouDto you
) {
    public record QuestionDto(
            UUID id,
            String text,
            List<AnswerDto> answers
    ) {}

    public record AnswerDto(
            UUID id,
            String text
    ) {}

    public record YouDto(
            int score,
            boolean answered
    ) {}
}
