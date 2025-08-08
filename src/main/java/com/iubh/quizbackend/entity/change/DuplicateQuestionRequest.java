package com.iubh.quizbackend.entity.change;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;

import java.util.UUID;

/**
 * A change request suggesting that this question is a duplicate of another.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DiscriminatorValue("DUPLICATE_QUESTION")
public class DuplicateQuestionRequest extends QuestionChangeRequest {

    /** The ID of the question this one is a duplicate of. */
    private UUID duplicateOfQuestionId;
}