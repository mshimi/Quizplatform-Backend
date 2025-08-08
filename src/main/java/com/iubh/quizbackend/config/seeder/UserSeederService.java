package com.iubh.quizbackend.config.seeder;

import com.iubh.quizbackend.entity.user.Profile;
import com.iubh.quizbackend.entity.user.Role;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserSeederService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> seedUsers() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Profile profile = Profile.builder()
                    .firstName("Test")
                    .name("User" + i)
                    .build();

            User user = User.builder()
                    .email("test" + i + "@test.com")
                    .password(passwordEncoder.encode("password"))
                    .profile(profile)
                    .role(Role.STUDENT)
                    .build();
            users.add(user);
        }
        return userRepository.saveAll(users);
    }
}