package com.iubh.quizbackend.entity.quiz;

import com.iubh.quizbackend.entity.question.ChoiceQuestion;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "session_questions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_session_question_index",
                        columnNames = {"session_id", "index_in_session"}
                )
        },
        indexes = {
                @Index(name = "ix_session_questions_session", columnList = "session_id")
        }
)
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"session", "question"})
public class SessionQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private LiveQuizSession session;

    /** 0..N, identisch für alle Teilnehmer. */
    @Column(name = "index_in_session", nullable = false)
    private int indexInSession;

    /** Referenz auf die „echte“ Frage. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private ChoiceQuestion question;

    /**
     * Reihenfolge der Antworten – für ALLE gleich.
     * Einfach als JSON (UUID-Array) ablegen, analog zu deinem QuizItem.
     */
    @Column(name = "answer_order_json", columnDefinition = "TEXT", nullable = false)
    private String answerOrderJson;
}
