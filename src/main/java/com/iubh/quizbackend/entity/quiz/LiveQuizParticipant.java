package com.iubh.quizbackend.entity.quiz;

import com.iubh.quizbackend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter

@NoArgsConstructor
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
    @Column(name = "score", nullable = false)
    private int score = 0;

    /** Nützlich für Abbruch/Grace-Logik. */
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
    private Set<ParticipantAnswer> answers = new HashSet<>();

    // --- Helper ---
    public void addAnswer(ParticipantAnswer a) {
        a.setParticipant(this);
        this.answers.add(a);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LiveQuizParticipant that)) return false;

        // If both have database IDs, compare those
        if (this.id != null && that.id != null) {
            return this.id.equals(that.id);
        }

        // Otherwise compare natural key (sessionId + userId)
        UUID thisSessionId = this.session != null ? this.session.getId() : null;
        UUID thatSessionId = that.session != null ? that.session.getId() : null;
        UUID thisUserId = this.user != null ? this.user.getId() : null;
        UUID thatUserId = that.user != null ? that.user.getId() : null;

        return Objects.equals(thisSessionId, thatSessionId)
               && Objects.equals(thisUserId, thatUserId);
    }

    @Override
    public int hashCode() {
        // If we have an id, use it; else use natural key
        if (id != null) return id.hashCode();
        UUID sessionId = session != null ? session.getId() : null;
        UUID userId = user != null ? user.getId() : null;
        return Objects.hash(sessionId, userId);
    }
}
