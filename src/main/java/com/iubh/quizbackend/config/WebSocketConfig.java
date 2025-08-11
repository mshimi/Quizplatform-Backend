package com.iubh.quizbackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private AuthChannelInterceptor authChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Topic prefixes that clients can subscribe to
        config.enableSimpleBroker("/topic");
        // Prefix for messages sent from client to server (e.g., /app/register)
        config.setApplicationDestinationPrefixes("/app");

        // should be routed to queues specific to a user.
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The endpoint clients use to connect to the WebSocket server
        registry.addEndpoint("/ws-connect")
                .setAllowedOriginPatterns("http://localhost:3000", "http://localhost:5173") // From your CorsConfig
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Register our custom interceptor to validate JWT on connect
        registration.interceptors(authChannelInterceptor);
    }
}