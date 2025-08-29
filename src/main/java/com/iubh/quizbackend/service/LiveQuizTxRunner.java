
// src/main/java/com/iubh/quizbackend/service/LiveQuizTxRunner.java
package com.iubh.quizbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iubh.quizbackend.api.dto.live.LiveEvents;
import com.iubh.quizbackend.entity.question.Answer;
import com.iubh.quizbackend.entity.quiz.LiveQuizSession;
import com.iubh.quizbackend.entity.quiz.SessionQuestion;
import com.iubh.quizbackend.entity.quiz.SessionStatus;
import com.iubh.quizbackend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LiveQuizTxRunner {

    private final LiveQuizSessionRepository sessionRepo;
    private final SessionQuestionRepository sessionQuestionRepo;
    private final LiveQuizParticipantRepository participantRepo;
    private final ParticipantAnswerRepository answerRepo;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    private String topic(UUID lobbyId) { return "/topic/lobby/" + lobbyId; }

    @Transactional
    public void showQuestion(UUID sessionId, int index) {
        LiveQuizSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));

        if (s.getStatus() == SessionStatus.CANCELLED || s.getStatus() == SessionStatus.FINISHED) return;
        if (index >= s.getTotalQuestions()) {
            finishSessionInternal(s);
            return;
        }

        s.setStatus(SessionStatus.RUNNING);
        s.setCurrentIndex(index);

        Instant endsAt = Instant.now().plusSeconds(s.getQuestionDurationSec());
        s.setQuestionEndsAt(endsAt);
        sessionRepo.save(s);

        SessionQuestion sq = sessionQuestionRepo.findWithQuestionAndAnswers(s.getId(), index)
                .orElseThrow(() -> new EntityNotFoundException("SessionQuestion not found"));

        List<UUID> orderIds;
        try {
            orderIds = Arrays.asList(objectMapper.readValue(sq.getAnswerOrderJson(), UUID[].class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("answerOrderJson malformed", e);
        }

        var answerMap = sq.getQuestion().getAnswers().stream()
                .collect(Collectors.toMap(Answer::getId, a -> a));

        var answers = orderIds.stream()
                .map(id -> {
                    var a = answerMap.get(id);
                    return new LiveEvents.QuestionShow.AnswerPayload(a.getId(), a.getText());
                })
                .toList();

        var qPayload = LiveEvents.QuestionShow.QuestionPayload.builder()
                .id(sq.getQuestion().getId())
                .text(sq.getQuestion().getQuestionText())
                .answers(answers)
                .build();

        var evt = LiveEvents.QuestionShow.builder()
                .sessionId(s.getId())
                .index(index)
                .endsAt(endsAt)
                .question(qPayload)
                .build();

        messagingTemplate.convertAndSend(topic(s.getLobbyId()), evt);
    }

    @Transactional
    public void endCurrentQuestion(UUID sessionId, boolean earlyAdvance) {
        LiveQuizSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));
        if (s.getStatus() != SessionStatus.RUNNING) return;

        int idx = s.getCurrentIndex();

        SessionQuestion sq = sessionQuestionRepo.findWithQuestionAndAnswers(s.getId(), idx)
                .orElseThrow(() -> new EntityNotFoundException("SessionQuestion not found"));

        UUID correctAnswerId = sq.getQuestion().getAnswers().stream()
                .filter(Answer::getIsCorrect)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Question without correct answer"))
                .getId();

        var leaderboard = participantRepo.getLeaderboard(s.getId()).stream()
                .map(r -> new LiveEvents.QuestionEnd.LeaderboardRow(r.getUserId(), r.getFirstName(), r.getName(), r.getScore()))
                .toList();

        var evt = LiveEvents.QuestionEnd.builder()
                .sessionId(s.getId())
                .index(idx)
                .correctAnswerId(correctAnswerId)
                .leaderboard(leaderboard)
                .build();

        messagingTemplate.convertAndSend(topic(s.getLobbyId()), evt);

        if (s.hasMoreQuestions()) {
            // next is scheduled by caller (LiveQuizService), we only persist timestamps here if needed
            s.setQuestionEndsAt(Instant.now().plus(s.getBufferDurationSec(), ChronoUnit.SECONDS));
            sessionRepo.save(s);
        } else {
            finishSessionInternal(s);
        }
    }

    @Transactional
    public void finishSessionInternal(LiveQuizSession s) {
        s.setStatus(SessionStatus.FINISHED);
        sessionRepo.save(s);

        var leaderboard = participantRepo.getLeaderboard(s.getId()).stream()
                .map(r -> new LiveEvents.QuestionEnd.LeaderboardRow(r.getUserId(), r.getFirstName(), r.getName(), r.getScore()))
                .toList();

        var evt = LiveEvents.QuizEnded.builder()
                .sessionId(s.getId())
                .leaderboard(leaderboard)
                .build();

        messagingTemplate.convertAndSend(topic(s.getLobbyId()), evt);
    }
}
