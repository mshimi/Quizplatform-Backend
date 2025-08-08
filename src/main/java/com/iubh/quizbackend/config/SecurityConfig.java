package com.iubh.quizbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables method-level security like @PreAuthorize
public class SecurityConfig {

    // You will need to create and inject this filter. It's responsible for validating the JWT token.
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, AuthenticationProvider authenticationProvider) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(@NonNull HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF protection, as we are using stateless JWT authentication.
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Define authorization rules for HTTP requests.
                .authorizeHttpRequests(auth -> auth
                        // Allow access to the React app's entry point and static assets
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/static/**",
                                "/*.ico",
                                "/*.png",
                                "/*.json",
                                "/*.js",
                                "/*.css"
                        ).permitAll()
                        // Allow access to public API endpoints for authentication
                        .requestMatchers(
                                "/api/v1/auth/authenticate",
                                "/api/v1/auth/register",
                                "/api/v1/auth/refresh-token"
                        ).permitAll()
                        // All other requests must be authenticated.
                        .anyRequest().authenticated()
                )

                // 3. Configure session management to be stateless.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Set the custom authentication provider.
                .authenticationProvider(authenticationProvider)

                // 5. Add the JWT authentication filter to run before the standard username/password filter.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}