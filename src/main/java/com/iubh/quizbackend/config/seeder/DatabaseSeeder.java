package com.iubh.quizbackend.config.seeder;

import com.iubh.quizbackend.entity.module.Module;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Main seeder class that orchestrates the database population process.
 * It runs on application startup and calls individual seeder services.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Profile("dev")


public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserSeederService userSeederService;
    private final ModuleSeederService moduleSeederService;
    private final QuestionSeederService questionSeederService;
    private final QuizSeederService quizSeederService;
  //  private final ChangeRequestSeederService changeRequestSeederService;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already contains data. Skipping data seeding.");
            return;
        }

        log.info("Starting database seeding process...");

        // 1. Seed Users
        List<User> users = userSeederService.seedUsers();
        log.info("Successfully seeded {} users.", users.size());

        // 2. Seed Modules
        List<Module> modules = moduleSeederService.seedModules();
        log.info("Successfully seeded {} modules.", modules.size());

        // 3. Seed Questions and Answers
        questionSeederService.seedQuestions(modules);
        log.info("Successfully seeded questions for modules.");

        // 4. Assign all modules to the first user
        assignModulesToFirstUser(users.get(0), modules);
        log.info("Assigned all {} modules to the first user: {}", modules.size(), users.get(0).getEmail());

        // 5. Seed Quiz attempts for the first user
        quizSeederService.seedQuizzes(users.get(0), modules);
        log.info("Successfully seeded quiz attempts for the first user.");

        // 6. Seed Change Requests from other users
       // changeRequestSeederService.seedChangeRequests(users, modules);
        log.info("Successfully seeded change requests.");

        log.info("Database seeding process complete.");
    }

    private void assignModulesToFirstUser(User user, List<Module> modules) {
        // We must fetch the user again to ensure it's a managed entity within this transaction
        User managedUser = userRepository.findById(user.getId()).orElseThrow();
        for (Module module : modules) {
            managedUser.followModule(module);
        }
        // Saving the user persists the many-to-many relationship
        userRepository.save(managedUser);
    }
}