package com.iubh.quizbackend.entity.change;


import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;

/**
 * A change request suggesting a correction to the question's text.
 */
@Data
@EqualsAndHashCode(callSuper = true) // Ensures parent fields are included in equals/hashCode
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DiscriminatorValue("INCORRECT_QUESTION_TEXT") // This value is stored in the 'request_type' column
public class IncorrectQuestionTextRequest extends QuestionChangeRequest {

    /** The new, corrected text for the question. */
    @Column(length = 1000)
    private String proposedText;
}
