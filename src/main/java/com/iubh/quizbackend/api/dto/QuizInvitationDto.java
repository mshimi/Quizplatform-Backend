package com.iubh.quizbackend.api.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class QuizInvitationDto {
    private UUID invitationId;
    private UUID moduleId;
    private UserDto inviter; // The user who sent the invitation
    private UserDto recipient; // The user who received it
    private InvitationStatus status;

    public enum InvitationStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        CANCELLED, // Cancelled by the inviter
        TIMED_OUT
    }
}