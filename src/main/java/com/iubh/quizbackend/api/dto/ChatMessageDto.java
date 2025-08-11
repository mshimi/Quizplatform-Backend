package com.iubh.quizbackend.api.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ChatMessageDto {
    private UUID senderId;
    private UUID recipientId;
    private String content;
    private LocalDateTime timestamp;
}