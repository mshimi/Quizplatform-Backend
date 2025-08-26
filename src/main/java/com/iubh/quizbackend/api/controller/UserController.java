package com.iubh.quizbackend.api.controller;

import com.iubh.quizbackend.api.dto.UpdateProfileRequestDto;
import com.iubh.quizbackend.api.dto.UserDto;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.mapper.UserMapper;
import com.iubh.quizbackend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * PUT /api/v1/user/profile : Updates the current user's profile.
     */
    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateProfileRequestDto requestDto
    ) {
        User updatedUser = userService.updateProfile(currentUser, requestDto);
        // Return the updated user information as a DTO
        return ResponseEntity.ok(UserMapper.toDto(updatedUser));
    }
}