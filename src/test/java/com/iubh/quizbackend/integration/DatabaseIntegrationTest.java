package com.iubh.quizbackend.integration;

import com.iubh.quizbackend.entity.module.Module;
import com.iubh.quizbackend.entity.question.Answer;
import com.iubh.quizbackend.entity.question.ChoiceQuestion;
import com.iubh.quizbackend.entity.user.Role;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.entity.user.Profile;
import com.iubh.quizbackend.repository.ChoiceQuestionRepository;
import com.iubh.quizbackend.repository.ModuleRepository;
import com.iubh.quizbackend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DatabaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private ChoiceQuestionRepository questionRepository;

    @Test
    void database_ShouldSupportBasicOperations() {
        // Database should be empty initially but functional
        long initialUserCount = userRepository.count();
        long initialModuleCount = moduleRepository.count();
        long initialQuestionCount = questionRepository.count();
        
        // All should be 0 since we don't seed in test
        assertThat(initialUserCount).isEqualTo(0);
        assertThat(initialModuleCount).isEqualTo(0);
        assertThat(initialQuestionCount).isEqualTo(0);
    }

    @Test
    void userRepository_ShouldSaveAndFindUsers() {
        // Create test user
        User testUser = User.builder()
                .email("db-test@test.com")
                .password("password")
                .role(Role.STUDENT)
                .profile(Profile.builder()
                        .firstName("Database")
                        .name("Test")
                        .build())
                .build();
        
        User savedUser = userRepository.save(testUser);
        
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("db-test@test.com");
        assertThat(savedUser.getRole()).isEqualTo(Role.STUDENT);
        assertThat(savedUser.getProfile()).isNotNull();
        
        // Find by email
        User foundUser = userRepository.findByEmail("db-test@test.com").orElse(null);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
    }

    @Test
    void moduleRepository_ShouldSaveAndFindModules() {
        // Create test module
        Module testModule = Module.builder()
                .title("Database Test Module")
                .description("Module for database testing")
                .build();
        
        Module savedModule = moduleRepository.save(testModule);
        
        assertThat(savedModule.getId()).isNotNull();
        assertThat(savedModule.getTitle()).isEqualTo("Database Test Module");
        assertThat(savedModule.getDescription()).isEqualTo("Module for database testing");
        
        // Find all modules should include our test module
        var modules = moduleRepository.findAll();
        assertThat(modules).hasSize(1);
        assertThat(modules.get(0).getTitle()).isEqualTo("Database Test Module");
    }

    @Test
    void questionRepository_ShouldSaveAndFindQuestions() {
        // First create a module
        Module testModule = moduleRepository.save(Module.builder()
                .title("Question Test Module")
                .description("Module for question testing")
                .build());
        
        // Create test question with answers
        ChoiceQuestion question = ChoiceQuestion.builder()
                .questionText("What is 2+2?")
                .module(testModule)
                .active(true)
                .build();
        
        Answer correctAnswer = Answer.builder()
                .text("4")
                .isCorrect(true)
                .question(question)
                .build();
        
        Answer wrongAnswer = Answer.builder()
                .text("5")
                .isCorrect(false)
                .question(question)
                .build();
        
        question.setAnswers(Set.of(correctAnswer, wrongAnswer));
        
        ChoiceQuestion savedQuestion = questionRepository.save(question);
        
        assertThat(savedQuestion.getId()).isNotNull();
        assertThat(savedQuestion.getQuestionText()).isEqualTo("What is 2+2?");
        assertThat(savedQuestion.getActive()).isTrue();
        assertThat(savedQuestion.getModule()).isEqualTo(testModule);
        assertThat(savedQuestion.getAnswers()).hasSize(2);
        
        // Find active questions
        var activeQuestions = questionRepository.findAll().stream()
                .filter(ChoiceQuestion::getActive)
                .toList();
        
        assertThat(activeQuestions).hasSize(1);
        assertThat(activeQuestions.get(0).getQuestionText()).isEqualTo("What is 2+2?");
    }

    @Test
    void userModuleRelationship_ShouldWorkCorrectly() {
        // Create test user and modules
        User testUser = userRepository.save(User.builder()
                .email("relationship-test@test.com")
                .password("password")
                .role(Role.STUDENT)
                .profile(Profile.builder()
                        .firstName("Relationship")
                        .name("Test")
                        .build())
                .build());
        
        Module module1 = moduleRepository.save(Module.builder()
                .title("Module 1")
                .description("First module")
                .build());
        
        Module module2 = moduleRepository.save(Module.builder()
                .title("Module 2")
                .description("Second module")
                .build());
        
        // Make user follow modules
        testUser.followModule(module1);
        testUser.followModule(module2);
        userRepository.save(testUser);
        
        // Verify relationships
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getFollowedModules()).hasSize(2);
        
        // Verify bidirectional relationship
        updatedUser.getFollowedModules().forEach(module -> {
            assertThat(module.getFollowers()).contains(updatedUser);
        });
    }

    @Test
    void repositories_ShouldSupportPagination() {
        // Create test modules
        for (int i = 1; i <= 5; i++) {
            moduleRepository.save(Module.builder()
                    .title("Pagination Module " + i)
                    .description("Module " + i + " for pagination testing")
                    .build());
        }
        
        // Test pagination
        var firstPage = moduleRepository.findAll(
            org.springframework.data.domain.PageRequest.of(0, 3)
        );
        
        assertThat(firstPage).isNotNull();
        assertThat(firstPage.getContent()).hasSize(3);
        assertThat(firstPage.getTotalElements()).isEqualTo(5);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        
        var secondPage = moduleRepository.findAll(
            org.springframework.data.domain.PageRequest.of(1, 3)
        );
        
        assertThat(secondPage.getContent()).hasSize(2);
        assertThat(secondPage.getTotalElements()).isEqualTo(5);
    }
}