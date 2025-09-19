package com.iubh.quizbackend.integration;

import com.iubh.quizbackend.api.dto.AnswerRequestDto;
import com.iubh.quizbackend.api.dto.CreateQuestionRequestDto;
import com.iubh.quizbackend.entity.module.Module;
import com.iubh.quizbackend.entity.question.ChoiceQuestion;
import com.iubh.quizbackend.entity.user.Role;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.entity.user.Profile;
import com.iubh.quizbackend.repository.ModuleRepository;
import com.iubh.quizbackend.repository.UserRepository;
import com.iubh.quizbackend.service.ModuleService;
import com.iubh.quizbackend.service.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ServiceIntegrationTest {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ModuleService moduleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModuleRepository moduleRepository;
    
    private User testUser1;
    private User testUser2;
    private Module testModule1;
    private Module testModule2;

    @BeforeEach
    void setUp() {
        // Create test users
        testUser1 = User.builder()
                .email("test1@integration.com")
                .password("password123")
                .role(Role.STUDENT)
                .profile(Profile.builder()
                        .firstName("Test")
                        .name("User1")
                        .build())
                .build();
        testUser1 = userRepository.save(testUser1);

        testUser2 = User.builder()
                .email("test2@integration.com")
                .password("password123")
                .role(Role.STUDENT)
                .profile(Profile.builder()
                        .firstName("Test")
                        .name("User2")
                        .build())
                .build();
        testUser2 = userRepository.save(testUser2);

        // Create test modules
        testModule1 = Module.builder()
                .title("Integration Test Module 1")
                .description("Test module for integration tests")
                .build();
        testModule1 = moduleRepository.save(testModule1);

        testModule2 = Module.builder()
                .title("Integration Test Module 2")
                .description("Another test module for integration tests")
                .build();
        testModule2 = moduleRepository.save(testModule2);
    }

    @Test
    void questionService_ShouldCreateQuestionWithAnswers() {
        // Use pre-created test module
        Module module = testModule1;
        
        // Create test question data
        List<AnswerRequestDto> answers = List.of(
            createAnswerRequest("Berlin", true),
            createAnswerRequest("Munich", false),
            createAnswerRequest("Hamburg", false)
        );

        CreateQuestionRequestDto request = CreateQuestionRequestDto.builder()
                .questionText("What is the capital of Germany?")
                .answers(answers)
                .build();

        // Create question through service
        ChoiceQuestion createdQuestion = questionService.createQuestion(module.getId(), request);

        // Verify question was created correctly
        assertThat(createdQuestion).isNotNull();
        assertThat(createdQuestion.getId()).isNotNull();
        assertThat(createdQuestion.getQuestionText()).isEqualTo("What is the capital of Germany?");
        assertThat(createdQuestion.getModule()).isEqualTo(module);
        assertThat(createdQuestion.getActive()).isTrue();
        assertThat(createdQuestion.getAnswers()).hasSize(3);

        // Verify answers are linked correctly
        createdQuestion.getAnswers().forEach(answer -> {
            assertThat(answer.getQuestion()).isEqualTo(createdQuestion);
        });

        // Verify one correct answer exists
        long correctAnswers = createdQuestion.getAnswers().stream()
                .mapToLong(answer -> answer.getIsCorrect() ? 1 : 0)
                .sum();
        assertThat(correctAnswers).isEqualTo(1);
    }

    @Test
    void questionService_ShouldSearchQuestionsWithPagination() {
        // First create a question for our test module
        List<AnswerRequestDto> answers = List.of(
            createAnswerRequest("Answer 1", true),
            createAnswerRequest("Answer 2", false)
        );
        CreateQuestionRequestDto request = CreateQuestionRequestDto.builder()
                .questionText("Test question for pagination?")
                .answers(answers)
                .build();
        questionService.createQuestion(testModule1.getId(), request);
        
        Module module = testModule1;

        // Search questions without filter
        var questionsPage = questionService.searchQuestions(
            module.getId(), 
            null, 
            PageRequest.of(0, 10)
        );

        assertThat(questionsPage).isNotNull();
        assertThat(questionsPage.getContent()).isNotEmpty();
        assertThat(questionsPage.getTotalElements()).isGreaterThan(0);
        
        // Verify all returned questions belong to the module
        questionsPage.getContent().forEach(questionDto -> {
            assertThat(questionDto.getId()).isNotNull();
            assertThat(questionDto.getQuestionText()).isNotBlank();
        });
    }

    @Test
    void moduleService_ShouldHandleUserFollowingWorkflow() {
        // Use pre-created test data
        User testUser = testUser1;
        Module testModule = testModule1;

        // Test toggle follow functionality
        boolean newFollowStatus1 = moduleService.toggleFollow(testModule.getId(), testUser);
        boolean newFollowStatus2 = moduleService.toggleFollow(testModule.getId(), testUser);

        // The two toggle operations should return opposite values
        assertThat(newFollowStatus1).isNotEqualTo(newFollowStatus2);
    }

    @Test
    void moduleService_ShouldReturnFollowedModules() {
        // Use pre-created test user and make them follow modules
        User testUser = testUser2;
        moduleService.toggleFollow(testModule1.getId(), testUser);
        moduleService.toggleFollow(testModule2.getId(), testUser);
        
        // Get followed modules
        var followedModulesPage = moduleService.getFollowedModules(
            testUser, 
            PageRequest.of(0, 10)
        );

        assertThat(followedModulesPage).isNotNull();
        assertThat(followedModulesPage.getTotalElements()).isEqualTo(2);
        
        followedModulesPage.getContent().forEach(moduleDto -> {
            assertThat(moduleDto.getId()).isNotNull();
            assertThat(moduleDto.getTitle()).isNotBlank();
            assertThat(moduleDto.getIsFollowed()).isTrue();
        });
    }

    private AnswerRequestDto createAnswerRequest(String text, boolean isCorrect) {
        AnswerRequestDto dto = new AnswerRequestDto();
        dto.setText(text);
        dto.setIsCorrect(isCorrect);
        return dto;
    }
}