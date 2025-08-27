package com.iubh.quizbackend.entity.change;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "proposed_answers")
@EqualsAndHashCode(of = "id")
@ToString(exclude = "changeRequest")
public class ProposedAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 1000)
    private String text;

    @Column(nullable = false)
    private Boolean isCorrect;

    // This links the proposed answer back to the specific change request it belongs to.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "change_request_id", nullable = false)
    private IncorrectAnswerRequest changeRequest;
}
