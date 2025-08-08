package com.iubh.quizbackend.entity.change;

import com.iubh.quizbackend.entity.question.Answer;
import jakarta.persistence.*;
import lombok.*;

/**
 * A change request suggesting a correction to one of the question's answers.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DiscriminatorValue("INCORRECT_ANSWER")
public class IncorrectAnswerRequest extends QuestionChangeRequest {

    /** The specific answer being targeted for a change. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_answer_id")
    private Answer targetAnswer;

    /** The new, corrected text for the answer. */
    @Column(length = 1000)
    private String proposedText;

    /** The proposed correctness for the answer. */
    private Boolean proposedIsCorrect;
}