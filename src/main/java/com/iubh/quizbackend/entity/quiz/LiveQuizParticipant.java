package com.iubh.quizbackend.entity.quiz;

import com.iubh.quizbackend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "live_quiz_participants",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_live_participant_session_user",
                        columnNames = {"session_id", "user_id"}
                )
        },
        indexes = {
                @Index(name = "ix_live_participants_session", columnList = "session_id"),
                @Index(name = "ix_live_participants_user", columnList = "user_id")
        }
)
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"session", "user", "answers"})
public class LiveQuizParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private LiveQuizSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Summierte Punkte (1/0) – für schnelles Leaderboard. */
    @Builder.Default
    @Column(name = "score", nullable = false)
    private int score = 0;

    /** Nützlich für Abbruch/Grace-Logik. */
    @Builder.Default
    @Column(name = "connected", nullable = false)
    private boolean connected = true;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @OneToMany(
            mappedBy = "participant",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private Set<ParticipantAnswer> answers = new HashSet<>();

    // --- Helper ---
    public void addAnswer(ParticipantAnswer a) {
        a.setParticipant(this);
        this.answers.add(a);
    }
}
