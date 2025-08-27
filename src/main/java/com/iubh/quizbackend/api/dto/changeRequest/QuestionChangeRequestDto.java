package com.iubh.quizbackend.api.dto.changeRequest;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.iubh.quizbackend.api.dto.ChoiceQuestionDto;
import com.iubh.quizbackend.entity.change.ChangeRequestStatus;
import com.iubh.quizbackend.entity.change.ChangeRequestType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base abstract DTO for all question change requests.
 * Uses Jackson annotations to handle polymorphic serialization/deserialization.
 * The 'requestType' field will determine which subclass is used.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY, // Use the existing 'requestType' property
        property = "requestType", // This field in the JSON determines the subclass
        visible = true // Make the 'requestType' field visible in the JSON output
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = QuestionChangeRequestDto.IncorrectQuestionTextRequestDto.class, name = "INCORRECT_QUESTION_TEXT"),
        @JsonSubTypes.Type(value = QuestionChangeRequestDto.IncorrectAnswerRequestDto.class, name = "INCORRECT_ANSWER"),
        @JsonSubTypes.Type(value = QuestionChangeRequestDto.SuggestDeletionRequestDto.class, name = "SUGGEST_DELETION"),
        @JsonSubTypes.Type(value = QuestionChangeRequestDto.DuplicateQuestionRequestDto.class, name = "DUPLICATE_QUESTION")
})
@Data
@NoArgsConstructor
public abstract class QuestionChangeRequestDto {

    // --- Common Fields for All Request Types ---
    private UUID id;
    private String requesterUsername;
    private ChangeRequestType requestType;
    private ChangeRequestStatus status;
    private String justification;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private ChoiceQuestionDto question;
    private long positiveVotes;
    private long negativeVotes;
    private boolean currentUserHasVoted;

    // --- Nested DTOs for Specific Request Types ---

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class IncorrectQuestionTextRequestDto extends QuestionChangeRequestDto {
        private String proposedText;
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class IncorrectAnswerRequestDto extends QuestionChangeRequestDto {
        private UUID targetAnswerId;
        private String oldAnswerText;
        private Boolean oldAnswerIsCorrect;
        private String proposedText;
        private Boolean proposedIsCorrect;
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class SuggestDeletionRequestDto extends QuestionChangeRequestDto {
        // No extra fields needed
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class DuplicateQuestionRequestDto extends QuestionChangeRequestDto {
        // The full DTO of the question that is claimed to be the original
        private ChoiceQuestionDto duplicateOfQuestion;
    }
}
