package com.iubh.quizbackend.entity.change;

import com.iubh.quizbackend.entity.question.ChoiceQuestion;
import com.iubh.quizbackend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Formula;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a user's suggestion to change or delete a question.
 * This is the abstract base class for a single-table inheritance strategy.
 */
@Getter // Using @Getter/@Setter on the parent is safer for inheritance than @Data
@Setter
@Entity
@Table(name = "question_change_requests")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "request_type", discriminatorType = DiscriminatorType.STRING)
@EqualsAndHashCode(exclude = {"question", "requester", "votes"})
@ToString(exclude = {"question", "requester", "votes"})
public abstract class QuestionChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id")
    private ChoiceQuestion question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id")
    private User requester;

    // The 'requestType' field is now handled by the @DiscriminatorColumn
    // and is no longer needed here.

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChangeRequestStatus status = ChangeRequestStatus.PENDING;

    @Column(nullable = false, length = 1000)
    private String justification;

    // --- Timestamps and Votes ---

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;



    @OneToMany(mappedBy = "changeRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChangeRequestVote> votes = new HashSet<>();

    public void addVote(ChangeRequestVote vote) {
        this.votes.add(vote);
        vote.setChangeRequest(this);
    }





}