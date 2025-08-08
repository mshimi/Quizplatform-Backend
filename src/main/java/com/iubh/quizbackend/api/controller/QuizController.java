package com.iubh.quizbackend.api.controller;

import com.iubh.quizbackend.api.dto.QuizSummaryDto;
import com.iubh.quizbackend.entity.quiz.QuizStatus;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
@Slf4j
public class QuizController {

    private final QuizService quizService;

    /**
     * GET /api/v1/quizzes : Get a paginated history of the current user's quiz attempts,
     * sorted by most recent.
     */
    @GetMapping
    public ResponseEntity<Page<QuizSummaryDto>> getMyQuizzes(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "4") Integer pageSize,
            @RequestParam(required = false) QuizStatus status
    ) {

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<QuizSummaryDto> quizzes = quizService.getQuizzesForUser(currentUser, pageable, status);
        return ResponseEntity.ok(quizzes);
    }
}