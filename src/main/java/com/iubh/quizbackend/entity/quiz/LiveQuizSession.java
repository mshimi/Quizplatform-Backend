package com.iubh.quizbackend.entity.quiz;

import com.iubh.quizbackend.entity.module.Module;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "live_quiz_sessions",
        indexes = {
                @Index(name = "ix_live_session_lobby", columnList = "lobby_id"),
                @Index(name = "ix_live_session_status", columnList = "status")
        }
)
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"module", "questions", "participants"})
public class LiveQuizSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Verknüpfung zur Lobby (soft/foreign – du hast bereits eine Lobby-Entity).
     * Falls du die Relation brauchst, kannst du hier statt UUID eine @ManyToOne auf QuizLobby machen.
     */
    @Column(name = "lobby_id", nullable = false)
    private UUID lobbyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status;

    /** Anzahl Fragen (z. B. 10) – eingefroren bei Start. */
    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;

    /** Index der aktuellen Frage (0..totalQuestions-1), -1 vor der ersten Frage. */
    @Builder.Default
    @Column(name = "current_index", nullable = false)
    private int currentIndex = -1;

    /** Countdown-Ziel: Alle Clients zählen bis zu diesem Zeitpunkt runter. */
    @Column(name = "start_at")
    private Instant startAt;

    /** Ende der aktuellen Frage (Server-Zeit als Quelle der Wahrheit). */
    @Column(name = "question_ends_at")
    private Instant questionEndsAt;

    /** Sek. pro Frage (z. B. 30). */
    @Builder.Default
    @Column(name = "question_duration_sec", nullable = false)
    private int questionDurationSec = 30;

    /** Sek. Buffer zwischen Fragen (z. B. 2). */
    @Builder.Default
    @Column(name = "buffer_duration_sec", nullable = false)
    private int bufferDurationSec = 2;

    /** Wenn true: frühzeitiger Wechsel sobald alle geantwortet haben. */
    @Builder.Default
    @Column(name = "early_advance_enabled", nullable = false)
    private boolean earlyAdvanceEnabled = true;

    @OneToMany(
            mappedBy = "session",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("indexInSession ASC")
    @Builder.Default
    private List<SessionQuestion> questions = new ArrayList<>();

    @OneToMany(
            mappedBy = "session",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private Set<LiveQuizParticipant> participants = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --- Helper ---
    public void addQuestion(SessionQuestion q) {
        q.setSession(this);
        this.questions.add(q);
    }

    public void addParticipant(LiveQuizParticipant p) {
        p.setSession(this);
        this.participants.add(p);
    }

    public boolean isRunning() {
        return status == SessionStatus.RUNNING || status == SessionStatus.COUNTDOWN;
    }

    public boolean hasMoreQuestions() {
        return currentIndex + 1 < totalQuestions;
    }
}
