package com.iubh.quizbackend.api.controller;

import com.iubh.quizbackend.api.dto.ChoiceQuestionDto;
import com.iubh.quizbackend.api.dto.CreateQuestionRequestDto;
import com.iubh.quizbackend.api.dto.question.QuestionSummaryDto;
import com.iubh.quizbackend.entity.question.ChoiceQuestion;
import com.iubh.quizbackend.mapper.ChoiceQuestionMapper;
import com.iubh.quizbackend.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final ChoiceQuestionMapper choiceQuestionMapper;

    /**
     * POST /api/v1/questions/{moduleId} : Creates a new question for a specific module.
     */
    @PostMapping("/{moduleId}")
    public ResponseEntity<ChoiceQuestionDto> createQuestionForModule(
            @PathVariable UUID moduleId,
            @RequestBody @Valid CreateQuestionRequestDto requestDto
    ) {
        ChoiceQuestion createdQuestion = questionService.createQuestion(moduleId, requestDto);
        ChoiceQuestionDto responseDto = choiceQuestionMapper.toDto(createdQuestion);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }


    @GetMapping("/search/{moduleId}")
    public ResponseEntity<Page<QuestionSummaryDto>> searchQuestions(
            @PathVariable UUID moduleId,
            @RequestParam(required = false) String searchTerm,
            Pageable pageable
    ) {
        Page<QuestionSummaryDto> questions = questionService.searchQuestions(moduleId, searchTerm, pageable);
        return ResponseEntity.ok(questions);
    }

}