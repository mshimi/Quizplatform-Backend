package com.iubh.quizbackend.api.dto;

import lombok.Data;
import java.util.UUID;

// Payload for when a user accepts or rejects an invitation
@Data
public class InvitationResponsePayload {
    private UUID invitationId;
    private boolean accepted;
}