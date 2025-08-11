package com.iubh.quizbackend.entity.quiz;

import com.iubh.quizbackend.entity.module.Module;
import com.iubh.quizbackend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a single quiz attempt by a user. It contains the set of questions
 * attempted and the overall status and result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quizzes")
@EqualsAndHashCode(exclude = {"user", "quizItems", "module"})
@ToString(exclude = {"user", "quizItems", "module"})
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(
            mappedBy = "quiz",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("questionOrder ASC") // This ensures the list is always fetched in the correct order
    @Builder.Default
    private Set<QuizItem> quizItems = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id")
    private Module module;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizStatus status;

    /**
     * The exact date and time this quiz was created/started.
     * This is automatically set by Hibernate when the entity is first saved.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // --- Result Methods (Derived, not stored in the database) ---

    /**
     * Calculates the total number of questions in this quiz.
     */
    public int getNumberOfQuestions() {
        return this.quizItems == null ? 0 : this.quizItems.size();
    }

    /**
     * Calculates the total number of correctly answered questions.
     */
    public long getNumberOfCorrectAnswers() {
        if (this.quizItems == null) return 0;
        return this.quizItems.stream()
                .filter(item -> Boolean.TRUE.equals(item.getIsCorrect()))
                .count();
    }

    /**
     * Calculates the total number of incorrectly answered questions.
     */
    public long getNumberOfWrongAnswers() {
        if (this.quizItems == null) return 0;
        return this.quizItems.stream()
                .filter(item -> Boolean.FALSE.equals(item.getIsCorrect()))
                .count();
    }

    // --- Helper method for bidirectional relationship ---
    public void addQuizItem(QuizItem item) {
        this.quizItems.add(item);
        item.setQuiz(this);
    }
}