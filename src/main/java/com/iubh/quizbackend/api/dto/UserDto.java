package com.iubh.quizbackend.api.dto;

import com.iubh.quizbackend.entity.user.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserDto {
   // private UUID id;
    private String firstName;
    private String name;
    private String email;
    private Role role;
}