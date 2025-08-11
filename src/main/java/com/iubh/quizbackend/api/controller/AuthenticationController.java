package com.iubh.quizbackend.api.controller;

import com.iubh.quizbackend.api.dto.AuthenticationRequest;
import com.iubh.quizbackend.api.dto.AuthenticationResponse;
import com.iubh.quizbackend.api.dto.RegisterRequest;
import com.iubh.quizbackend.api.dto.UserDto;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.mapper.UserMapper;
import com.iubh.quizbackend.service.AuthenticationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request,
            HttpServletResponse response // Inject response to set the cookie
    ) {
        AuthenticationResponse authResponse = service.register(request);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());

        // Return only the access token in the body
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .accessToken(authResponse.getAccessToken())
                .build());
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request,
            HttpServletResponse response // Inject response to set the cookie
    ) {
        log.debug("Authenticating request");
        log.debug("Request: {}", request);
        AuthenticationResponse authResponse = service.authenticate(request);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());

        // Return only the access token in the body
        return ResponseEntity.ok(AuthenticationResponse.builder()
                .accessToken(authResponse.getAccessToken())
                .build());
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            // A much cleaner way to get the cookie value
            @CookieValue(name = "refresh_token") String refreshToken
    ) {
        return ResponseEntity.ok(service.refreshToken(refreshToken));
    }

    /**
     * Endpoint to get the currently authenticated user's profile information.
     * @param principal The CustomUserDetails object injected by Spring Security.
     * @return A DTO with the user's details.
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(
            @AuthenticationPrincipal User principal
    ) {
        // The principal is our CustomUserDetails, so we can call getUser()
        return ResponseEntity.ok(UserMapper.toDto(principal));
    }

    /**
     * Endpoint to log the user out by clearing their refresh token cookie.
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        clearRefreshTokenCookie(response);
        return ResponseEntity.ok("Logout successful.");
    }


    /**
     * Helper method to create and set the refresh token cookie.
     * @param response The HttpServletResponse to add the cookie to.
     * @param refreshToken The refresh token string.
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);  // Prevents access from JavaScript
        cookie.setSecure(true);    // Sent only over HTTPS
        cookie.setPath("/api/v1/auth"); // Scope the cookie to your auth endpoints
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        // cookie.setSameSite("Strict"); // Recommended for extra CSRF protection
        response.addCookie(cookie);
    }

    /**
     * Helper method to clear the refresh token cookie upon logout.
     */
    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(0); // Expire the cookie immediately
        response.addCookie(cookie);
    }


}