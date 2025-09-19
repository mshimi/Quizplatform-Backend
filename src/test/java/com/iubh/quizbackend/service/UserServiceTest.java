package com.iubh.quizbackend.service;

import com.iubh.quizbackend.api.dto.UpdateProfileRequestDto;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Profile testProfile;
    private UpdateProfileRequestDto updateRequest;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testProfile = Profile.builder()
                .firstName("John")
                .name("Doe")
                .build();

        testUser = User.builder()
                .id(userId)
                .email("john.doe@test.com")
                .password("encodedPassword")
                .profile(testProfile)
                .role(Role.STUDENT)
                .build();

        updateRequest = new UpdateProfileRequestDto();
        updateRequest.setFirstName("Jane");
        updateRequest.setName("Smith");
    }

    @Test
    void updateProfile_WithValidData_ShouldUpdateUserProfile() {
        // Given
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.updateProfile(testUser, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testUser);
        assertThat(result.getProfile().getFirstName()).isEqualTo("Jane");
        assertThat(result.getProfile().getName()).isEqualTo("Smith");

        verify(userRepository).save(testUser);
    }

    @Test
    void updateProfile_WithNullFirstName_ShouldUpdateOnlyName() {
        // Given
        updateRequest.setFirstName(null);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.updateProfile(testUser, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProfile().getFirstName()).isNull();
        assertThat(result.getProfile().getName()).isEqualTo("Smith");

        verify(userRepository).save(testUser);
    }

    @Test
    void updateProfile_WithNullName_ShouldUpdateOnlyFirstName() {
        // Given
        updateRequest.setName(null);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.updateProfile(testUser, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProfile().getFirstName()).isEqualTo("Jane");
        assertThat(result.getProfile().getName()).isNull();

        verify(userRepository).save(testUser);
    }

    @Test
    void updateProfile_ShouldPreserveOtherUserProperties() {
        // Given
        String originalEmail = testUser.getEmail();
        String originalPassword = testUser.getPassword();
        Role originalRole = testUser.getRole();

        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.updateProfile(testUser, updateRequest);

        // Then
        assertThat(result.getEmail()).isEqualTo(originalEmail);
        assertThat(result.getPassword()).isEqualTo(originalPassword);
        assertThat(result.getRole()).isEqualTo(originalRole);

        verify(userRepository).save(testUser);
    }

    @Test
    void updateProfile_ShouldCreateNewProfileInstance() {
        // Given
        Profile originalProfile = testUser.getProfile();
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.updateProfile(testUser, updateRequest);

        // Then
        assertThat(result.getProfile()).isNotSameAs(originalProfile);
        assertThat(result.getProfile().getFirstName()).isEqualTo("Jane");
        assertThat(result.getProfile().getName()).isEqualTo("Smith");

        verify(userRepository).save(testUser);
    }
}