package com.iubh.quizbackend.entity.quiz;

import com.iubh.quizbackend.entity.question.Answer;
import com.iubh.quizbackend.entity.question.ChoiceQuestion;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a single question within a specific quiz instance,
 * including the user's selected answer(s) and the timestamp of the answer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quiz_items")
@EqualsAndHashCode(exclude = {"quiz", "question", "selectedAnswers"})
@ToString(exclude = {"quiz", "question", "selectedAnswers"})
public class QuizItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    private ChoiceQuestion question;

    /**
     * The specific answer options the user selected for this question in this quiz.
     * This is a ManyToMany relationship because a user can select multiple answers for a MULTI_CHOICE question.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_answer_id")
    private Answer selectedAnswer;

    @Column(name = "shuffled_answer_order", columnDefinition = "TEXT")
    private String shuffledAnswerOrder;

    /**
     * The exact date and time the user submitted their answer for this question.
     */
    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    /**
     * Stores whether the user's submitted answer was correct.
     * This is calculated and set by the service layer when the answer is submitted.
     */
    @Column(name = "is_correct")
    private Boolean isCorrect;


    @Column(name = "question_order", nullable = false)
    private int questionOrder;
}