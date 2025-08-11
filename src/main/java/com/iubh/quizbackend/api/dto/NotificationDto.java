package com.iubh.quizbackend.api.dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto<T> {

    private NotificationType type;
    private T payload;

    public enum NotificationType {
        QUIZ_INVITATION,
        CHAT_MESSAGE
        // You can add more types later
        // FRIEND_REQUEST,
        // SYSTEM_ALERT
    }
}
