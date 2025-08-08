package com.iubh.quizbackend.service;

// ... other imports
import com.iubh.quizbackend.api.dto.AuthenticationRequest;
import com.iubh.quizbackend.api.dto.AuthenticationResponse;
import com.iubh.quizbackend.api.dto.RegisterRequest;
import com.iubh.quizbackend.entity.user.Profile;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.entity.user.Role;
import com.iubh.quizbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // No longer needs HttpServletResponse
    public AuthenticationResponse register(RegisterRequest request) {
        var profile = Profile.builder()
                .firstName(request.getFirstName())
                .name(request.getName())
                .build();
        var user = User.builder()
                .profile(profile)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STUDENT)
                .build();
        userRepository.save(user);

        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        // Return both tokens in the DTO
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // No longer needs HttpServletResponse
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        // Return both tokens in the DTO
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // Now accepts the token string directly
    public AuthenticationResponse refreshToken(String refreshToken) {
        final String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            var user = this.userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if (jwtService.isTokenValid(refreshToken, user)) {
                var newAccessToken = jwtService.generateToken(user);
                // Only the new access token is needed in the response
                return AuthenticationResponse.builder()
                        .accessToken(newAccessToken)
                        .build();
            }
        }
        throw new IllegalArgumentException("Invalid refresh token");
    }
}