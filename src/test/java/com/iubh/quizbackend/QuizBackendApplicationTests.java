package com.iubh.quizbackend;

import com.iubh.quizbackend.repository.UserRepository;
import com.iubh.quizbackend.repository.ModuleRepository;
import com.iubh.quizbackend.repository.ChoiceQuestionRepository;
import com.iubh.quizbackend.service.AuthenticationService;
import com.iubh.quizbackend.service.ModuleService;
import com.iubh.quizbackend.service.QuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class QuizBackendApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ModuleRepository moduleRepository;
    
    @Autowired
    private ChoiceQuestionRepository choiceQuestionRepository;
    
    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private ModuleService moduleService;
    
    @Autowired
    private QuestionService questionService;

    @Test
    void contextLoads() {
        // Verify Spring application context loads successfully
        assertThat(applicationContext).isNotNull();
    }
    
    @Test
    void repositoryBeansAreLoaded() {
        // Verify all repository beans are properly loaded
        assertThat(userRepository).isNotNull();
        assertThat(moduleRepository).isNotNull();
        assertThat(choiceQuestionRepository).isNotNull();
    }
    
    @Test
    void serviceBeansAreLoaded() {
        // Verify all service beans are properly loaded
        assertThat(authenticationService).isNotNull();
        assertThat(moduleService).isNotNull();
        assertThat(questionService).isNotNull();
    }
    
    @Test
    void databaseConnectionWorks() {
        // Verify database connection is established
        long userCount = userRepository.count();
        long moduleCount = moduleRepository.count();
        long questionCount = choiceQuestionRepository.count();
        
        // In test profile, database should be empty initially
        assertThat(userCount).isEqualTo(0);
        assertThat(moduleCount).isEqualTo(0);
        assertThat(questionCount).isEqualTo(0);
    }

}
