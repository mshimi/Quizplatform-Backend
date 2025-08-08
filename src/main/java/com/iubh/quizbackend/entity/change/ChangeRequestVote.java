package com.iubh.quizbackend.entity.change;

import com.iubh.quizbackend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "change_request_votes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"change_request_id", "voter_id"})
})
@EqualsAndHashCode(exclude = {"changeRequest", "voter"})
@ToString(exclude = {"changeRequest", "voter"})
public class ChangeRequestVote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "change_request_id")
    private QuestionChangeRequest changeRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "voter_id")
    private User voter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteType voteType;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime votedAt;
}