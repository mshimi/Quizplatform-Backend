// src/main/java/com/iubh/quizbackend/api/controller/LiveQuizController.java
package com.iubh.quizbackend.api.controller;

import com.iubh.quizbackend.api.dto.live.SessionStateDto;
import com.iubh.quizbackend.api.dto.live.StartLiveSessionResponseDto;
import com.iubh.quizbackend.api.dto.live.SubmitLiveAnswerDto;
import com.iubh.quizbackend.entity.quiz.LiveQuizSession;
import com.iubh.quizbackend.entity.quiz.SessionStatus;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.service.LiveQuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LiveQuizController {

    private final LiveQuizService liveQuizService;

    /**
     * Host startet das Live-Quiz für eine Lobby.
     *
     * POST /api/v1/lobbies/{lobbyId}/start
     */
    @PostMapping("/lobbies/{lobbyId}/start")
    public ResponseEntity<StartLiveSessionResponseDto> startLiveSession(
            @PathVariable UUID lobbyId,
            @AuthenticationPrincipal User currentUser
    ) {
        LiveQuizSession s = liveQuizService.startSession(currentUser, lobbyId);
        var dto = new StartLiveSessionResponseDto(
                s.getId(),
                s.getStartAt(),
                s.getTotalQuestions(),
                s.getQuestionDurationSec(),
                s.getBufferDurationSec()
        );
        return ResponseEntity.ok(dto);
    }

    /**
     * Antwort eines Teilnehmers für die aktuelle Frage.
     *
     * POST /api/v1/sessions/{sessionId}/answers
     * Body: { questionIndex, answerId }
     */
    @PostMapping("/sessions/{sessionId}/answers")
    public ResponseEntity<Void> submitAnswer(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody SubmitLiveAnswerDto body
    ) {
        liveQuizService.submitAnswer(sessionId, currentUser, body.questionIndex(), body.answerId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Snapshot/State für Reconnect oder Initialisierung des Clients.
     *
     * GET /api/v1/sessions/{sessionId}/state
     */
    @GetMapping("/sessions/{sessionId}/state")
    public ResponseEntity<SessionStateDto> getState(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal User currentUser
    ) {
        Map<String, Object> raw = liveQuizService.getSessionState(sessionId, currentUser);

        var status = SessionStatus.valueOf((String) raw.get("status"));

        int currentIndex = Optional.ofNullable((Number) raw.get("currentIndex"))
                .map(Number::intValue).orElse(-1);
        int totalQuestions = Optional.ofNullable((Number) raw.get("totalQuestions"))
                .map(Number::intValue).orElse(0);

        Instant startAt = (Instant) raw.get("startAt");
        Instant endsAt  = (Instant) raw.get("endsAt");

        SessionStateDto.QuestionDto qDto = null;
        @SuppressWarnings("unchecked")
        Map<String, Object> qMap = (Map<String, Object>) raw.get("question");
        if (qMap != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> ans = (List<Map<String, Object>>) qMap.getOrDefault("answers", List.of());
            var answers = ans.stream()
                    .map(a -> new SessionStateDto.AnswerDto(
                            (UUID) a.get("id"),
                            (String) a.get("text")))
                    .toList();

            qDto = new SessionStateDto.QuestionDto(
                    (UUID) qMap.get("id"),
                    (String) qMap.get("text"),
                    answers
            );
        }


        @SuppressWarnings("unchecked")
        Map<String, Object> youMap = (Map<String, Object>) raw.get("you");
        var youDto = new SessionStateDto.YouDto(
                (Integer) youMap.get("score"),
                (Boolean) youMap.get("answered")
        );

        var dto = new SessionStateDto(
                status,
                currentIndex,
                totalQuestions,
                startAt,
                endsAt,
                qDto,
                youDto
        );

        return ResponseEntity.ok(dto);
    }
}
