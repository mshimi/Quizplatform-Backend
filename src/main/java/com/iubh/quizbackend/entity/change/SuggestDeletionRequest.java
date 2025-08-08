package com.iubh.quizbackend.entity.change;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * A change request suggesting that the entire question should be deleted.
 * This class has no extra fields.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Entity
@DiscriminatorValue("SUGGEST_DELETION")
public class SuggestDeletionRequest extends QuestionChangeRequest {
    // No additional fields are needed for this type of request.
    // The justification is stored in the parent class.
}