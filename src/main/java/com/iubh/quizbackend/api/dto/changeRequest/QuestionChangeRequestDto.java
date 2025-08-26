package com.iubh.quizbackend.api.dto.changeRequest;


import com.iubh.quizbackend.entity.change.ChangeRequestStatus;
import com.iubh.quizbackend.entity.change.ChangeRequestType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class QuestionChangeRequestDto {
    private UUID id;
    private UUID questionId;
    private String questionText;
    private String requesterUsername;
    private ChangeRequestType requestType;
    private ChangeRequestStatus status;
    private String justification;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    // Fields for specific request types
    private String proposedText; // For IncorrectQuestionTextRequest & IncorrectAnswerRequest
    private Boolean proposedIsCorrect; // For IncorrectAnswerRequest
    private UUID duplicateOfQuestionId; // For DuplicateQuestionRequest
    private UUID targetAnswerId; // For IncorrectAnswerRequest
}
