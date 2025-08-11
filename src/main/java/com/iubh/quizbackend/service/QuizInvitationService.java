package com.iubh.quizbackend.service;

import com.iubh.quizbackend.api.dto.NotificationDto;
import com.iubh.quizbackend.api.dto.QuizInvitationDto;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.mapper.UserMapper;
import com.iubh.quizbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizInvitationService {

    // A thread-safe map to store pending invitations. Key is invitationId.
    private final ConcurrentHashMap<UUID, QuizInvitationDto> pendingInvitations = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    /**
     * Creates an invitation and sends it to the recipient.
     */
    public void createAndSendInvitation(User inviter, UUID recipientId, UUID moduleId) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        QuizInvitationDto invitation = QuizInvitationDto.builder()
                .invitationId(UUID.randomUUID())
                .moduleId(moduleId)
                .inviter(UserMapper.toDto(inviter))
                .recipient(UserMapper.toDto(recipient))
                .status(QuizInvitationDto.InvitationStatus.PENDING)
                .build();

        pendingInvitations.put(invitation.getInvitationId(), invitation);

        // Send the private message to the recipient's personal queue
        sendInvitationUpdate(invitation);

        // Schedule the timeout task
        scheduler.schedule(() -> timeoutInvitation(invitation.getInvitationId()), 60, TimeUnit.SECONDS);
        log.info("Invitation {} sent from {} to {}. Timeout scheduled.", invitation.getInvitationId(), inviter.getEmail(), recipient.getEmail());
    }

    /**
     * Handles a user's response (accept or reject) to an invitation.
     */
    public void handleResponse(UUID invitationId, boolean accepted) {
        QuizInvitationDto invitation = pendingInvitations.remove(invitationId);
        if (invitation == null) {
            log.warn("Response received for an unknown or already handled invitation: {}", invitationId);
            return;
        }

        invitation.setStatus(accepted ? QuizInvitationDto.InvitationStatus.ACCEPTED : QuizInvitationDto.InvitationStatus.REJECTED);
        sendInvitationUpdate(invitation);
        log.info("Invitation {} was {}.", invitation.getInvitationId(), invitation.getStatus());
        // Here you would add logic to start the quiz if accepted
    }

    /**
     * Allows the original inviter to cancel the invitation.
     */
    public void cancelInvitation(UUID invitationId) {
        QuizInvitationDto invitation = pendingInvitations.remove(invitationId);
        if (invitation == null) return;

        invitation.setStatus(QuizInvitationDto.InvitationStatus.CANCELLED);
        sendInvitationUpdate(invitation);
        log.info("Invitation {} was cancelled by the inviter.", invitationId);
    }

    /**
     * Automatically times out an invitation if no response is received.
     */
    private void timeoutInvitation(UUID invitationId) {
        // The 'remove' operation is atomic. If it returns non-null, it means we successfully
        // removed it, and it wasn't handled by another method in the meantime.
        QuizInvitationDto invitation = pendingInvitations.remove(invitationId);
        if (invitation != null) {
            invitation.setStatus(QuizInvitationDto.InvitationStatus.TIMED_OUT);
            sendInvitationUpdate(invitation);
            log.info("Invitation {} timed out.", invitationId);
        }
    }

    /**
     * A helper method to send updates to both the inviter and recipient.
     */
    private void sendInvitationUpdate(QuizInvitationDto invitation) {
        // The destination is now the generic notification queue for both users.
        String destination = "/queue/notification";

        // Wrap the invitation DTO in our generic Notification object.
        NotificationDto<QuizInvitationDto> notification = new NotificationDto<>(
                NotificationDto.NotificationType.QUIZ_INVITATION,
                invitation
        );

        // Send to the inviter
        messagingTemplate.convertAndSendToUser(invitation.getInviter().getEmail(), destination, notification);
        // Send to the recipient
        messagingTemplate.convertAndSendToUser(invitation.getRecipient().getEmail(), destination, notification);
    }
}