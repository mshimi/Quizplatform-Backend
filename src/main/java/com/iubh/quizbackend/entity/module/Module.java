package com.iubh.quizbackend.entity.module;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.iubh.quizbackend.entity.question.ChoiceQuestion;
import com.iubh.quizbackend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "modules")
@EqualsAndHashCode(exclude = {"followers", "questions"})
@ToString(exclude = {"followers", "questions"})

public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String title;

    @Column(length = 1000) // Allow for a longer description
    private String description;

    @ManyToMany(mappedBy = "followedModules", fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore // Prevents infinite loops during JSON serialization
    private Set<User> followers = new HashSet<>();


    @OneToMany(
            mappedBy = "module", // This links to the 'module' field in the ChoiceQuestion entity
            cascade = CascadeType.ALL,
            orphanRemoval = true // Ensures questions are deleted if the module is deleted
    )
    @Builder.Default
    @JsonIgnore // Ignore during serialization to prevent fetching a potentially large list
    private Set<ChoiceQuestion> questions = new HashSet<>();

    @Formula("(select count(q.id) from choice_questions q where q.module_id = id)")
    private int numberOfChoiceQuestions;



    // ADD THIS: A highly efficient, read-only property for the like count.
    @Formula("(select count(ufm.user_id) from user_followed_modules ufm where ufm.module_id = id)")
    private int likeCount;

    // @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<Quiz> quizzes = new ArrayList<>();
}