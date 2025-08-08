package com.iubh.quizbackend.entity.change;

public enum ChangeRequestType {
    /**
     * The text of the question itself is incorrect or unclear.
     */
    INCORRECT_QUESTION_TEXT,

    /**
     * An answer option is wrong, or its correctness (true/false) is wrong.
     */
    INCORRECT_ANSWER,

    /**
     * The entire question is irrelevant, nonsensical, or should be removed.
     */
    SUGGEST_DELETION,

    /**
     * This question is a duplicate of another existing question.
     */
    DUPLICATE_QUESTION
}