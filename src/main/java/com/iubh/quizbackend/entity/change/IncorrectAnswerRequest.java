package com.iubh.quizbackend.entity.change;

import com.iubh.quizbackend.entity.question.Answer;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A change request suggesting a correction to one of the question's answers.
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DiscriminatorValue("INCORRECT_ANSWER")
public class IncorrectAnswerRequest extends QuestionChangeRequest {

    @OneToMany(
            mappedBy = "changeRequest",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ProposedAnswer> proposedAnswers = new ArrayList<>();

    // --- HELPER METHOD ---
    public void addProposedAnswer(ProposedAnswer answer) {
        this.proposedAnswers.add(answer);
        answer.setChangeRequest(this);
    }
}