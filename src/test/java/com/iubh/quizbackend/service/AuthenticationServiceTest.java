package com.iubh.quizbackend.service;

import com.iubh.quizbackend.api.dto.AuthenticationRequest;
import com.iubh.quizbackend.api.dto.AuthenticationResponse;
import com.iubh.quizbackend.api.dto.RegisterRequest;
import com.iubh.quizbackend.entity.user.Profile;
import com.iubh.quizbackend.entity.user.Role;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private RegisterRequest registerRequest;
    private AuthenticationRequest authRequest;

    @BeforeEach
    void setUp() {
        Profile profile = Profile.builder()
                .firstName("John")
                .name("Doe")
                .build();

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("john.doe@test.com")
                .password("encodedPassword")
                .profile(profile)
                .role(Role.STUDENT)
                .build();

        registerRequest = RegisterRequest.builder()
                .firstName("John")
                .name("Doe")
                .email("john.doe@test.com")
                .password("password123")
                .build();

        authRequest = AuthenticationRequest.builder()
                .email("john.doe@test.com")
                .password("password123")
                .build();
    }

    @Test
    void register_ShouldCreateUserAndReturnTokens() {
        // Given
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");

        // When
        AuthenticationResponse response = authenticationService.register(registerRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
        verify(jwtService).generateRefreshToken(any(User.class));
    }

    @Test
    void authenticate_WithValidCredentials_ShouldReturnTokens() {
        // Given
        when(userRepository.findByEmail("john.doe@test.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(testUser)).thenReturn("refreshToken");

        // When
        AuthenticationResponse response = authenticationService.authenticate(authRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("john.doe@test.com");
        verify(jwtService).generateToken(testUser);
        verify(jwtService).generateRefreshToken(testUser);
    }

    @Test
    void authenticate_WithInvalidCredentials_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail("john.doe@test.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticate(authRequest))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("john.doe@test.com");
        verify(jwtService, never()).generateToken(any());
        verify(jwtService, never()).generateRefreshToken(any());
    }

    @Test
    void refreshToken_WithValidToken_ShouldReturnNewAccessToken() {
        // Given
        String refreshToken = "validRefreshToken";
        when(jwtService.extractUsername(refreshToken)).thenReturn("john.doe@test.com");
        when(userRepository.findByEmail("john.doe@test.com")).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid(refreshToken, testUser)).thenReturn(true);
        when(jwtService.generateToken(testUser)).thenReturn("newAccessToken");

        // When
        AuthenticationResponse response = authenticationService.refreshToken(refreshToken);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("newAccessToken");
        assertThat(response.getRefreshToken()).isNull();

        verify(jwtService).extractUsername(refreshToken);
        verify(userRepository).findByEmail("john.doe@test.com");
        verify(jwtService).isTokenValid(refreshToken, testUser);
        verify(jwtService).generateToken(testUser);
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldThrowException() {
        // Given
        String refreshToken = "invalidRefreshToken";
        when(jwtService.extractUsername(refreshToken)).thenReturn("john.doe@test.com");
        when(userRepository.findByEmail("john.doe@test.com")).thenReturn(Optional.of(testUser));
        when(jwtService.isTokenValid(refreshToken, testUser)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authenticationService.refreshToken(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid refresh token");

        verify(jwtService).extractUsername(refreshToken);
        verify(userRepository).findByEmail("john.doe@test.com");
        verify(jwtService).isTokenValid(refreshToken, testUser);
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void refreshToken_WithNullUsername_ShouldThrowException() {
        // Given
        String refreshToken = "tokenWithNullUsername";
        when(jwtService.extractUsername(refreshToken)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> authenticationService.refreshToken(refreshToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid refresh token");

        verify(jwtService).extractUsername(refreshToken);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void refreshToken_WithUserNotFound_ShouldThrowException() {
        // Given
        String refreshToken = "validRefreshToken";
        when(jwtService.extractUsername(refreshToken)).thenReturn("nonexistent@test.com");
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authenticationService.refreshToken(refreshToken))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");

        verify(jwtService).extractUsername(refreshToken);
        verify(userRepository).findByEmail("nonexistent@test.com");
    }
}