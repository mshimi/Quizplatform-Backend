package com.iubh.quizbackend.service;

import com.iubh.quizbackend.api.dto.ChatMessageDto;
import com.iubh.quizbackend.api.dto.NotificationDto;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public void sendMessage(User sender, ChatMessageDto chatMessage) {
        // Here you would add logic to save the message to the database
        // e.g., chatMessageRepository.save(message);
        log.info("Sending chat message from {} to {}", sender.getId(), chatMessage.getRecipientId());

        // Set the timestamp on the server for accuracy
        chatMessage.setTimestamp(LocalDateTime.now());

        // Wrap the chat message in our generic notification DTO
        NotificationDto<ChatMessageDto> notification = new NotificationDto<>(
                NotificationDto.NotificationType.CHAT_MESSAGE,
                chatMessage
        );

        // Fetch the recipient's user details to get their username (email)
        userRepository.findById(chatMessage.getRecipientId()).ifPresent(recipient -> {
            // Send the private message to the recipient's notification queue
            messagingTemplate.convertAndSendToUser(
                    recipient.getEmail(),
                    "/queue/notification",
                    notification
            );
        });
    }
}