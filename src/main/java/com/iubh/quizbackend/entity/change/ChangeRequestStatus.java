package com.iubh.quizbackend.entity.change;

public enum ChangeRequestStatus {
    /**
     * The request is open and waiting for user votes.
     */
    PENDING,

    /**
     * The request has been accepted by the community or an admin.
     * The suggested change should be applied.
     */
    APPROVED,

    /**
     * The request has been rejected by the community or an admin.
     */
    REJECTED
}
