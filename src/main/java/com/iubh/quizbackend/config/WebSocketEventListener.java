package com.iubh.quizbackend.config;

import com.iubh.quizbackend.api.controller.WebSocketController;
import com.iubh.quizbackend.entity.module.Module;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.repository.ModuleRepository;
import com.iubh.quizbackend.repository.UserRepository;
import com.iubh.quizbackend.service.ActiveUserStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ActiveUserStore activeUserStore;
    private final WebSocketController webSocketController;
    private final UserRepository userRepository;
    private final ModuleRepository moduleRepository;

    @Transactional(readOnly = true)
    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();

        if (principal == null) return;

        String destination = headerAccessor.getDestination();
        if (destination != null && destination.startsWith("/user/queue/notification")) {

            // --- THE FIX IS HERE ---
            // 1. Get the security token from the principal
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
            // 2. Get your User entity from inside the token
            User currentUser = (User) token.getPrincipal();

            String sessionId = headerAccessor.getSessionId();

            log.info("User {} is now online (Session: {}).", currentUser.getEmail(), sessionId);
            activeUserStore.addUser(sessionId, currentUser.getId());



            List<Module> followedModules = moduleRepository.findModulesFollowedByUserId(currentUser.getId());
            for (Module followedModule : followedModules) {
                webSocketController.broadcastUpdate(followedModule.getId());
            }
        }
    }

    @Transactional(readOnly = true)
    @EventListener

    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        UUID removedUserId = activeUserStore.removeUser(sessionId);

        if (removedUserId != null) {
            log.info("User with ID {} disconnected.", removedUserId);

            // --- APPLY THE SAME FIX HERE for consistency and safety ---
            List<Module> followedModules = moduleRepository.findModulesFollowedByUserId(removedUserId);
            for (Module followedModule : followedModules) {
                webSocketController.broadcastUpdate(followedModule.getId());
            }
        }
    }

}