package com.iubh.quizbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull; // Import the NonNull annotation

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/api/**") // Apply CORS to all endpoints under /api
                        .allowedOrigins(
                                "http://localhost:3000",
                                "http://localhost:5173",
                                "http://ec2-13-51-207-0.eu-north-1.compute.amazonaws.com:8081",
                                "https://ec2-13-51-207-0.eu-north-1.compute.amazonaws.com"

                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allowed HTTP methods
                        .allowedHeaders("*") // Allow all headers
                        .allowCredentials(true); // Allow cookies and credentials

                registry.addMapping("/ws-connect/**")
                        .allowedOrigins(
                                "http://localhost:5173",
                                "http://ec2-13-51-207-0.eu-north-1.compute.amazonaws.com:8081",
                                "https://ec2-13-51-207-0.eu-north-1.compute.amazonaws.com"

                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowCredentials(true); // Allow cookies and credentials;
            }
        };
    }
}
