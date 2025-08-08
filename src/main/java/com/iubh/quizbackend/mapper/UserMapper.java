package com.iubh.quizbackend.mapper;

import com.iubh.quizbackend.api.dto.UserDto;
import com.iubh.quizbackend.entity.user.Profile;
import com.iubh.quizbackend.entity.user.User;

public class UserMapper {

    public static UserDto toDto(User user) {
        final Profile profile = user.getProfile();
        return UserDto
                .builder()
                .name(profile.getName())
                .firstName(profile.getFirstName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

}
