package com.iubh.quizbackend.entity.quiz;

import com.iubh.quizbackend.entity.question.Answer;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "participant_answers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_answer_participant_idx",
                        columnNames = {"participant_id", "question_index"}
                )
        },
        indexes = {
                @Index(name = "ix_answers_participant", columnList = "participant_id"),
                @Index(name = "ix_answers_session_idx", columnList = "session_id,question_index")
        }
)
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"participant", "answer"})
public class ParticipantAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Zur Beschleunigung mancher Queries speichern wir die Session-ID direkt mit. */
    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id", nullable = false)
    private LiveQuizParticipant participant;

    /** Index der Frage innerhalb der Session (0..N). */
    @Column(name = "question_index", nullable = false)
    private int questionIndex;

    /** Gewählte Antwort. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "answer_id", nullable = false)
    private Answer answer;

    /** Serverseitig berechnetes Flag. */
    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;
}
