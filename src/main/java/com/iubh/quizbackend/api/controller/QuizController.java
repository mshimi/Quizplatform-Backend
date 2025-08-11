package com.iubh.quizbackend.api.controller;

import com.iubh.quizbackend.api.dto.QuizDetailDto;
import com.iubh.quizbackend.api.dto.QuizResultDto;
import com.iubh.quizbackend.api.dto.QuizSummaryDto;
import com.iubh.quizbackend.api.dto.SubmitAnswerRequestDto;
import com.iubh.quizbackend.entity.quiz.Quiz;
import com.iubh.quizbackend.entity.quiz.QuizStatus;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.mapper.QuizMapper;
import com.iubh.quizbackend.repository.QuizRepository;
import com.iubh.quizbackend.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
@Slf4j
public class QuizController {

    private final QuizService quizService;

    private final QuizMapper quizMapper;



    /**
     * POST /api/v1/quizzes/start/{moduleId} : Startet ein neues Einzelspieler-Quiz.
     */
    @PostMapping("/start/{moduleId}")
    public ResponseEntity<QuizDetailDto> startNewQuiz(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID moduleId
    ) {
        Quiz newQuiz = quizService.startQuiz(currentUser, moduleId);
        // Wir benötigen eine neue Mapper-Methode, um zur QuizDetailDto zu konvertieren
        QuizDetailDto quizDto = quizMapper.toDetailDto(newQuiz);
        return new ResponseEntity<>(quizDto, HttpStatus.CREATED);
    }


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


    /**
     * GET /api/v1/quizzes/{quizId} : Fetches a specific quiz.
     * The response format depends on the quiz's status.
     */
    @GetMapping("/{quizId}")
    public ResponseEntity<?> getQuiz(
            @PathVariable UUID quizId,
            @AuthenticationPrincipal User currentUser
    ) throws AccessDeniedException {
        Object quizObject = quizService.getQuizById(quizId, currentUser);

        // Check the type of the returned object to send the correct DTO
        if (quizObject instanceof Quiz) {
            // If the quiz is IN_PROGRESS, map to the secure detail DTO
            QuizDetailDto dto = quizMapper.toDetailDto((Quiz) quizObject);
            return ResponseEntity.ok(dto);
        } else if (quizObject instanceof QuizResultDto) {
            // If the quiz is COMPLETED, return the result DTO
            return ResponseEntity.ok(quizObject);
        }

        // Fallback for an unexpected type
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }


    @PostMapping("/{quizId}/submit-answer")
    public ResponseEntity<Void> submitAnswer(
            @PathVariable UUID quizId,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody SubmitAnswerRequestDto submitAnswerRequestDto
    ) throws AccessDeniedException {
        quizService.submitAnswer(quizId, currentUser, submitAnswerRequestDto);
        // Antwortet mit 204 No Content, da das Frontend nicht auf eine Antwort wartet
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{quizId}/finish")
    public ResponseEntity<Void> finishQuiz(
            @PathVariable UUID quizId,
            @AuthenticationPrincipal User currentUser
    ) throws AccessDeniedException {
        quizService.finishQuiz(quizId, currentUser);
        return ResponseEntity.ok().build();
    }

}