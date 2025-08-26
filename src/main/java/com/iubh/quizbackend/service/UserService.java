package com.iubh.quizbackend.service;

import com.iubh.quizbackend.api.dto.UpdateProfileRequestDto;
import com.iubh.quizbackend.entity.user.Profile;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Updates the profile information for the currently authenticated user.
     *
     * @param currentUser The user entity from the security context.
     * @param requestDto The DTO containing the new profile data.
     * @return The updated User entity.
     */
    @Transactional
    public User updateProfile(User currentUser, UpdateProfileRequestDto requestDto) {
        // Since 'Profile' is an @Embeddable, we can create a new instance
        // and set it on the user. JPA will handle the update.
        Profile updatedProfile = Profile.builder()
                .firstName(requestDto.getFirstName())
                .name(requestDto.getName())
                .build();

        currentUser.setProfile(updatedProfile);

        // Save the user entity with the updated embedded profile
        return userRepository.save(currentUser);
    }
}