package com.iubh.quizbackend.entity.quiz;

/**
 * Represents the lifecycle status of a user's quiz attempt.
 */
public enum QuizStatus {
    /**
     * The quiz has been started but not yet completed.
     */
    IN_PROGRESS,

    /**
     * The user has finished the quiz and the result is final.
     */
    COMPLETED
}