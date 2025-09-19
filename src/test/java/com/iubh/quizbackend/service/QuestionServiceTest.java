package com.iubh.quizbackend.service;

import com.iubh.quizbackend.api.dto.AnswerDto;
import com.iubh.quizbackend.api.dto.AnswerRequestDto;
import com.iubh.quizbackend.api.dto.CreateQuestionRequestDto;
import com.iubh.quizbackend.api.dto.question.QuestionSummaryDto;
import com.iubh.quizbackend.entity.module.Module;
import com.iubh.quizbackend.entity.question.Answer;
import com.iubh.quizbackend.entity.question.ChoiceQuestion;
import com.iubh.quizbackend.mapper.ChoiceQuestionMapper;
import com.iubh.quizbackend.repository.ChoiceQuestionRepository;
import com.iubh.quizbackend.repository.ModuleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private ChoiceQuestionRepository choiceQuestionRepository;

    @Mock
    private ModuleRepository moduleRepository;

    @Mock
    private ChoiceQuestionMapper choiceQuestionMapper;

    @InjectMocks
    private QuestionService questionService;

    private Module testModule;
    private ChoiceQuestion testQuestion;
    private CreateQuestionRequestDto createRequest;
    private UUID moduleId;

    @BeforeEach
    void setUp() {
        moduleId = UUID.randomUUID();
        
        testModule = Module.builder()
                .id(moduleId)
                .title("Test Module")
                .description("Test Description")
                .questions(new HashSet<>())
                .build();

        testQuestion = ChoiceQuestion.builder()
                .id(UUID.randomUUID())
                .questionText("What is 2+2?")
                .active(true)
                .module(testModule)
                .answers(new HashSet<>())
                .build();

        List<AnswerRequestDto> answers = List.of(
                createAnswerRequestDto("3", false),
                createAnswerRequestDto("4", true),
                createAnswerRequestDto("5", false)
        );

        createRequest = CreateQuestionRequestDto.builder()
                .questionText("What is 2+2?")
                .answers(answers)
                .build();
    }

    @Test
    void createQuestion_WithValidData_ShouldCreateAndReturnQuestion() {
        // Given
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(testModule));
        when(choiceQuestionRepository.save(any(ChoiceQuestion.class))).thenReturn(testQuestion);

        // When
        ChoiceQuestion result = questionService.createQuestion(moduleId, createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getQuestionText()).isEqualTo("What is 2+2?");
        assertThat(result.getModule()).isEqualTo(testModule);
        assertThat(result.getActive()).isTrue();

        // Verify repository interactions
        verify(moduleRepository).findById(moduleId);
        
        ArgumentCaptor<ChoiceQuestion> questionCaptor = ArgumentCaptor.forClass(ChoiceQuestion.class);
        verify(choiceQuestionRepository).save(questionCaptor.capture());
        
        ChoiceQuestion capturedQuestion = questionCaptor.getValue();
        assertThat(capturedQuestion.getQuestionText()).isEqualTo("What is 2+2?");
        assertThat(capturedQuestion.getModule()).isEqualTo(testModule);
        assertThat(capturedQuestion.getAnswers()).hasSize(3);
        
        // Check that answers were added correctly
        List<Answer> capturedAnswers = capturedQuestion.getAnswers().stream().toList();
        assertThat(capturedAnswers).hasSize(3);
        assertThat(capturedAnswers.stream().anyMatch(a -> a.getText().equals("4") && a.getIsCorrect())).isTrue();
        assertThat(capturedAnswers.stream().filter(a -> !a.getIsCorrect())).hasSize(2);
    }

    @Test
    void createQuestion_WithNonExistentModule_ShouldThrowException() {
        // Given
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> questionService.createQuestion(moduleId, createRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Module not found with id: " + moduleId);

        verify(moduleRepository).findById(moduleId);
        verify(choiceQuestionRepository, never()).save(any());
    }

    @Test
    void searchQuestions_WithoutSearchTerm_ShouldReturnAllActiveQuestions() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<ChoiceQuestion> questions = List.of(testQuestion);
        Page<ChoiceQuestion> questionPage = new PageImpl<>(questions, pageable, 1);
        
        QuestionSummaryDto summaryDto = QuestionSummaryDto.builder()
                .id(testQuestion.getId())
                .questionText(testQuestion.getQuestionText())
                .build();
        
        when(choiceQuestionRepository.findByModule_IdAndActiveTrue(moduleId, pageable))
                .thenReturn(questionPage);
        when(choiceQuestionMapper.toSummaryDto(testQuestion)).thenReturn(summaryDto);

        // When
        Page<QuestionSummaryDto> result = questionService.searchQuestions(moduleId, null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(summaryDto);

        verify(choiceQuestionRepository).findByModule_IdAndActiveTrue(moduleId, pageable);
        verify(choiceQuestionRepository, never())
                .findByModule_IdAndQuestionTextContainingIgnoreCaseAndActiveTrue(any(), any(), any());
        verify(choiceQuestionMapper).toSummaryDto(testQuestion);
    }

    @Test
    void searchQuestions_WithEmptySearchTerm_ShouldReturnAllActiveQuestions() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<ChoiceQuestion> questions = List.of(testQuestion);
        Page<ChoiceQuestion> questionPage = new PageImpl<>(questions, pageable, 1);
        
        QuestionSummaryDto summaryDto = QuestionSummaryDto.builder()
                .id(testQuestion.getId())
                .questionText(testQuestion.getQuestionText())
                .build();
        
        when(choiceQuestionRepository.findByModule_IdAndActiveTrue(moduleId, pageable))
                .thenReturn(questionPage);
        when(choiceQuestionMapper.toSummaryDto(testQuestion)).thenReturn(summaryDto);

        // When
        Page<QuestionSummaryDto> result = questionService.searchQuestions(moduleId, "   ", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(choiceQuestionRepository).findByModule_IdAndActiveTrue(moduleId, pageable);
        verify(choiceQuestionRepository, never())
                .findByModule_IdAndQuestionTextContainingIgnoreCaseAndActiveTrue(any(), any(), any());
    }

    @Test
    void searchQuestions_WithSearchTerm_ShouldReturnFilteredQuestions() {
        // Given
        String searchTerm = "math";
        Pageable pageable = PageRequest.of(0, 10);
        List<ChoiceQuestion> questions = List.of(testQuestion);
        Page<ChoiceQuestion> questionPage = new PageImpl<>(questions, pageable, 1);
        
        QuestionSummaryDto summaryDto = QuestionSummaryDto.builder()
                .id(testQuestion.getId())
                .questionText(testQuestion.getQuestionText())
                .build();
        
        when(choiceQuestionRepository.findByModule_IdAndQuestionTextContainingIgnoreCaseAndActiveTrue(
                moduleId, searchTerm, pageable)).thenReturn(questionPage);
        when(choiceQuestionMapper.toSummaryDto(testQuestion)).thenReturn(summaryDto);

        // When
        Page<QuestionSummaryDto> result = questionService.searchQuestions(moduleId, searchTerm, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(summaryDto);

        verify(choiceQuestionRepository).findByModule_IdAndQuestionTextContainingIgnoreCaseAndActiveTrue(
                moduleId, searchTerm, pageable);
        verify(choiceQuestionRepository, never()).findByModule_IdAndActiveTrue(any(), any());
        verify(choiceQuestionMapper).toSummaryDto(testQuestion);
    }

    @Test
    void searchQuestions_WithEmptyResults_ShouldReturnEmptyPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ChoiceQuestion> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        
        when(choiceQuestionRepository.findByModule_IdAndActiveTrue(moduleId, pageable))
                .thenReturn(emptyPage);

        // When
        Page<QuestionSummaryDto> result = questionService.searchQuestions(moduleId, null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(choiceQuestionRepository).findByModule_IdAndActiveTrue(moduleId, pageable);
        verify(choiceQuestionMapper, never()).toSummaryDto(any());
    }

    private AnswerRequestDto createAnswerRequestDto(String text, boolean isCorrect) {
        AnswerRequestDto dto = new AnswerRequestDto();
        dto.setText(text);
        dto.setIsCorrect(isCorrect);
        return dto;
    }

    @Test
    void createQuestion_ShouldSetBidirectionalRelationships() {
        // Given
        when(moduleRepository.findById(moduleId)).thenReturn(Optional.of(testModule));
        when(choiceQuestionRepository.save(any(ChoiceQuestion.class))).thenAnswer(invocation -> {
            ChoiceQuestion question = invocation.getArgument(0);
            question.setId(UUID.randomUUID());
            return question;
        });

        // When
        ChoiceQuestion result = questionService.createQuestion(moduleId, createRequest);

        // Then
        ArgumentCaptor<ChoiceQuestion> questionCaptor = ArgumentCaptor.forClass(ChoiceQuestion.class);
        verify(choiceQuestionRepository).save(questionCaptor.capture());
        
        ChoiceQuestion capturedQuestion = questionCaptor.getValue();
        
        // Verify that all answers have the question set (bidirectional relationship)
        capturedQuestion.getAnswers().forEach(answer -> {
            assertThat(answer.getQuestion()).isEqualTo(capturedQuestion);
        });
    }
}