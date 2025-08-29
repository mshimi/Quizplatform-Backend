package com.iubh.quizbackend.api.dto.live;

// src/main/java/com/iubh/quizbackend/api/dto/live/LiveEvents.java


import lombok.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LiveEvents {

    public interface LiveEvent { String getType(); }

    @Getter @Builder @AllArgsConstructor
    public static class QuizStarted implements LiveEvent {
        @Builder.Default private final String type = "QUIZ_STARTED";
        private final java.util.UUID lobbyId;
        private final java.util.UUID sessionId;
        private final java.time.Instant startAt;
        private final int totalQuestions;
        private final int questionDurationSec;
        private final int bufferDurationSec;
    }

    @Getter @Builder @AllArgsConstructor
    public static class QuestionShow implements LiveEvent {
        @Builder.Default private final String type = "QUESTION_SHOW";
        private final java.util.UUID sessionId;
        private final int index;
        private final java.time.Instant endsAt;
        private final QuestionPayload question;
        @Getter @Builder @AllArgsConstructor
        public static class QuestionPayload {
            private final java.util.UUID id;
            private final String text;
            private final java.util.List<AnswerPayload> answers;
        }
        @Getter @Builder @AllArgsConstructor
        public static class AnswerPayload {
            private final java.util.UUID id;
            private final String text;
        }
    }

    @Getter @Builder @AllArgsConstructor
    public static class QuestionEnd implements LiveEvent {
        @Builder.Default private final String type = "QUESTION_END";
        private final java.util.UUID sessionId;
        private final int index;
        private final java.util.UUID correctAnswerId;
        private final java.util.List<LeaderboardRow> leaderboard;
        @Getter @Builder @AllArgsConstructor
        public static class LeaderboardRow {
            private final java.util.UUID userId;
            private final String firstName;
            private final String name;
            private final int score;
        }
    }

    @Getter @Builder @AllArgsConstructor
    public static class QuizEnded implements LiveEvent {
        @Builder.Default private final String type = "QUIZ_ENDED";
        private final java.util.UUID sessionId;
        private final java.util.List<QuestionEnd.LeaderboardRow> leaderboard;
    }

    @Getter @Builder @AllArgsConstructor
    public static class QuizAborted implements LiveEvent {
        @Builder.Default private final String type = "QUIZ_ABORTED";
        private final java.util.UUID sessionId;
        private final String reason;
    }
    }

