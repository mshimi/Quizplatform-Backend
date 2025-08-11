package com.iubh.quizbackend.api.controller;

import com.iubh.quizbackend.api.dto.ChatMessageDto;
import com.iubh.quizbackend.api.dto.InvitationResponsePayload;
import com.iubh.quizbackend.api.dto.UserDto;
import com.iubh.quizbackend.entity.module.Module;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.mapper.UserMapper;
import com.iubh.quizbackend.repository.UserRepository;
import com.iubh.quizbackend.service.ActiveUserStore;
import com.iubh.quizbackend.service.ChatService;
import com.iubh.quizbackend.service.QuizInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ActiveUserStore activeUserStore;
    private final UserRepository userRepository;

//    /**
//     * When a client connects and sends a message here, it signifies they are "online".
//     * This method doesn't need a payload, as the user's identity is from the token.
//     */
//    @MessageMapping("/register")
//    @Transactional(readOnly = true) // Important for lazily fetching followedModules
//    public void register(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
//        User currentUser = (User) principal;
//        String sessionId = headerAccessor.getSessionId();
//
//        // 1. Add user to the central online list
//        activeUserStore.addUser(sessionId, currentUser.getId());
//
//        // 2. Trigger an update for EVERY module this user follows
//        for (Module followedModule : currentUser.getFollowedModules()) {
//            broadcastUpdate(followedModule.getId());
//        }
//    }

    /**
     * Finds all active users for a SINGLE module and broadcasts the list to that module's topic.
     * This method will be called multiple times by the register and disconnect handlers.
     */
    public void broadcastUpdate(UUID moduleId) {
        if (moduleId == null) return;

        // 1. Get all user IDs that are currently online
        Collection<UUID> onlineUserIds = activeUserStore.getOnlineUserIds();
        if (onlineUserIds.isEmpty()) {
            // Optimization: if no one is online, send an empty list and stop.
            messagingTemplate.convertAndSend("/topic/activeUsers/" + moduleId, Collections.emptyList());
            return;
        }

        // 2. From the online users, find which ones follow the specified module
        List<User> activeFollowers = userRepository.findActiveUsersFollowingModule(
                new ArrayList<>(onlineUserIds),
                moduleId
        );

        // 3. Map to DTOs for the client
        List<UserDto> activeFollowersDto = activeFollowers.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());

        // 4. Send the targeted update
        String destination = "/topic/activeUsers/" + moduleId;
        messagingTemplate.convertAndSend(destination, activeFollowersDto);
    }



    private final QuizInvitationService invitationService; // Inject the new service

    // DTO for the invitation payload from the client
    private record QuizInvitePayload(UUID recipientId, UUID moduleId) {}

    /**
     * Alice sends a message here to invite Alex.
     */
    @MessageMapping("/quiz/invite")
    public void inviteUser(@Payload QuizInvitePayload payload, Principal principal) {
        // --- APPLY THE SAME FIX HERE ---
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        User currentUser = (User) token.getPrincipal();

        invitationService.createAndSendInvitation(currentUser, payload.recipientId(), payload.moduleId());
    }

    /**
     * Alex sends a message here to accept or reject Alice's invitation.
     */
    @MessageMapping("/quiz/invitation/respond")
    public void respondToInvitation(@Payload InvitationResponsePayload payload) {
        invitationService.handleResponse(payload.getInvitationId(), payload.isAccepted());
    }

    /**
     * Alice sends a message here to cancel the invitation she sent to Alex.
     */
    @MessageMapping("/quiz/invitation/cancel")
    public void cancelInvitation(@Payload UUID invitationId) {
        invitationService.cancelInvitation(invitationId);
    }

    private final ChatService chatService; // Inject the new ChatService

    @MessageMapping("/chat/send")
    public void sendChatMessage(@Payload ChatMessageDto chatMessage, Principal principal) {
        // --- AND APPLY THE FIX HERE ---
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        User currentUser = (User) token.getPrincipal();

        chatMessage.setSenderId(currentUser.getId());
        chatService.sendMessage(currentUser, chatMessage);
    }

}