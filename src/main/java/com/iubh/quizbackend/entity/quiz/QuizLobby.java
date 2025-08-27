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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quiz_lobbies")
@EqualsAndHashCode(exclude = {"host", "module", "participants"})
@ToString(exclude = {"host", "module", "participants"})
public class QuizLobby {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_id")
    private User host;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id")
    private Module module;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "quiz_lobby_participants",
            joinColumns = @JoinColumn(name = "quiz_lobby_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> participants = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizLobbyStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
