package com.iubh.quizbackend.service;

import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class ActiveUserStore {

    // A thread-safe map: Key is the WebSocket session ID, Value is the user's ID.
    private final ConcurrentHashMap<String, UUID> onlineUsers = new ConcurrentHashMap<>();

    public void addUser(String sessionId, UUID userId) {
        onlineUsers.put(sessionId, userId);
    }

    public UUID removeUser(String sessionId) {
        return onlineUsers.remove(sessionId);
    }

    /**
     * Returns a collection of all unique IDs of currently online users.
     * This is a snapshot at the time of calling.
     */
    public Collection<UUID> getOnlineUserIds() {
        if (onlineUsers.isEmpty()) {
            return Collections.emptyList();
        }
        // The values of the map are the user IDs.
        return onlineUsers.values();
    }
}