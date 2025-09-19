// src/main/java/com/iubh/quizbackend/service/LiveQuizService.java
package com.iubh.quizbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iubh.quizbackend.api.dto.live.LiveEvents;
import com.iubh.quizbackend.entity.module.Module;
import com.iubh.quizbackend.entity.question.Answer;
import com.iubh.quizbackend.entity.question.ChoiceQuestion;
import com.iubh.quizbackend.entity.quiz.*;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveQuizService {

    private final LiveQuizSessionRepository sessionRepo;
    private final SessionQuestionRepository sessionQuestionRepo;
    private final LiveQuizParticipantRepository participantRepo;
    private final ParticipantAnswerRepository answerRepo;

    private final QuizLobbyRepository lobbyRepo;
    private final ChoiceQuestionRepository choiceQuestionRepo;

    private final SimpMessagingTemplate messagingTemplate;
    private final TaskScheduler taskScheduler;
    private final ObjectMapper objectMapper;


    private final LiveQuizTxRunner tx;


    // Scheduler-Futures pro Session (für EndsAt / Buffer)
    private final Map<UUID, ScheduledFuture<?>> scheduledEnds = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledFuture<?>> scheduledNext = new ConcurrentHashMap<>();


    // private final @Lazy LiveQuizService self;
    private final PlatformTransactionManager txManager; // <-- ADD


    private void runInTx(Runnable action) {
        new TransactionTemplate(txManager).executeWithoutResult(status -> action.run());
    }

    // Leichtgewichtige Locks pro Session
    private final Map<UUID, Object> sessionLocks = new ConcurrentHashMap<>();

    private Object lock(UUID sessionId) {
        return sessionLocks.computeIfAbsent(sessionId, id -> new Object());
    }

    private String topicForLobby(UUID lobbyId) {
        return "/topic/lobby/" + lobbyId;
    }

    // ---------------------- Start & Aufbau ----------------------


    /**
     * Host startet die Session (aus der Lobby).
     * - friert 10 Fragen + Antwortreihenfolge ein
     * - baut Teilnehmerliste aus Lobby
     * - setzt startAt = now + 5s
     * - broadcast QUIZ_STARTED
     */
    @Transactional
    public LiveQuizSession startSession(User host, UUID lobbyId) {
        var lobby = lobbyRepo.findById(lobbyId)
                .orElseThrow(() -> new EntityNotFoundException("Lobby not found: " + lobbyId));

        if (!lobby.getHost().getId().equals(host.getId())) {
            throw new SecurityException("Only host can start the quiz.");
        }
        if (lobby.getStatus() != QuizLobbyStatus.WAITING) {
            throw new IllegalStateException("Lobby already started or cancelled.");
        }

        lobby.setStatus(QuizLobbyStatus.IN_PROGRESS);
        lobbyRepo.save(lobby);

        final int QUESTION_COUNT = 10;  // TODO: Change to 10 or handle in HTTP Request
        Module module = lobby.getModule();

        List<ChoiceQuestion> picked =
                choiceQuestionRepo
                        .pickRandomByModule(module.getId(), PageRequest.of(0, QUESTION_COUNT))
                        .getContent();


        if (picked.isEmpty()) {
            throw new IllegalStateException("Not enough questions in module.");
        }

        LiveQuizSession session = LiveQuizSession.builder()
                .lobbyId(lobby.getId())
                .module(module)
                .status(SessionStatus.COUNTDOWN)
                .totalQuestions(QUESTION_COUNT)
                .questionDurationSec(30)
                .bufferDurationSec(2)
                .earlyAdvanceEnabled(true)
                .startAt(Instant.now().plusSeconds(5))
                .currentIndex(-1)
                .build();

        // Persist & ab hier nur noch savedSession benutzen (effektiv final)
        final LiveQuizSession savedSession = sessionRepo.save(session);

        int idx = 0;
        for (ChoiceQuestion q : picked) {
            List<Answer> answers = new ArrayList<>(q.getAnswers());
            Collections.shuffle(answers);

            List<UUID> orderIds = answers.stream().map(Answer::getId).toList();
            final String orderJson;
            try {
                orderJson = objectMapper.writeValueAsString(orderIds);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Could not serialize answer order.", e);
            }

            SessionQuestion sq = SessionQuestion.builder()
                    .session(savedSession)
                    .indexInSession(idx++)
                    .question(q)
                    .answerOrderJson(orderJson)
                    .build();
            savedSession.addQuestion(sq);
        }
        sessionQuestionRepo.saveAll(savedSession.getQuestions());

        // Teilnehmer eintragen (Lambda darf savedSession verwenden, weil final)
        lobby.getParticipants().forEach(u -> {
            log.info(u.getId().toString());

            LiveQuizParticipant lp = new LiveQuizParticipant();
            lp.setSession(savedSession);
            lp.setConnected(true);
            lp.setUser(u);
            lp.setJoinedAt(java.time.LocalDateTime.now());
            lp.setScore(0);

            savedSession.addParticipant(lp);
        });
        participantRepo.saveAll(savedSession.getParticipants());

        var evt = LiveEvents.QuizStarted.builder()
                .lobbyId(lobby.getId())
                .sessionId(savedSession.getId())
                .startAt(savedSession.getStartAt())
                .totalQuestions(savedSession.getTotalQuestions())
                .questionDurationSec(savedSession.getQuestionDurationSec())
                .bufferDurationSec(savedSession.getBufferDurationSec())
                .build();

        // messagingTemplate.convertAndSend("/topic/lobby/" + lobby.getId(), evt);

        org.springframework.transaction.support.TransactionSynchronizationManager
                .registerSynchronization(new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        messagingTemplate.convertAndSend(topicForLobby(lobby.getId()), evt);
                    }
                });

        scheduleNextQuestionAt(savedSession.getId(), savedSession.getStartAt(), 0);

        return savedSession;
    }
    // --------------------- Scheduling Helpers ---------------------

    private void scheduleNextQuestionAt(UUID sessionId, Instant when, int indexToShow) {
        cancelFuture(scheduledNext.remove(sessionId));
        var fut = taskScheduler.schedule(() -> {
            synchronized (lock(sessionId)) {
                tx.showQuestion(sessionId, indexToShow);
            }
        }, Date.from(when));
        scheduledNext.put(sessionId, fut);
    }

    private void scheduleEndAt(UUID sessionId, Instant when) {
        cancelFuture(scheduledEnds.remove(sessionId));
        var fut = taskScheduler.schedule(() -> {
            synchronized (lock(sessionId)) {
                tx.endCurrentQuestion(sessionId, false);
            }
        }, Date.from(when));
        scheduledEnds.put(sessionId, fut);
    }

    private void cancelFuture(ScheduledFuture<?> f) {
        if (f != null) {
            try {
                f.cancel(false);
            } catch (Exception ignored) {
            }
        }
    }

    // ------------------ Show & End Question ------------------

    @org.springframework.transaction.annotation.Transactional
    public void safeShowQuestion(UUID sessionId, int index) {
        synchronized (lock(sessionId)) {
            showQuestion(sessionId, index); // this internal call now runs WITH an active TX
        }
    }

    @Transactional
    protected void showQuestion(UUID sessionId, int index) {
        LiveQuizSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));

        if (s.getStatus() == SessionStatus.CANCELLED || s.getStatus() == SessionStatus.FINISHED) {
            return;
        }
        if (index >= s.getTotalQuestions()) {
            // Nichts mehr zu zeigen → finish
            finishSessionInternal(s);
            return;
        }

        s.setStatus(SessionStatus.RUNNING);
        s.setCurrentIndex(index);

        Instant endsAt = Instant.now().plusSeconds(s.getQuestionDurationSec());
        s.setQuestionEndsAt(endsAt);
        sessionRepo.save(s);

        // Payload der Frage erzeugen (in Session-Order)
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

        List<LiveEvents.QuestionShow.AnswerPayload> answers = orderIds.stream()
                .map(id -> {
                    Answer a = answerMap.get(id);
                    return new LiveEvents.QuestionShow.AnswerPayload(a.getId(), a.getText());
                })
                .collect(Collectors.toList());

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
        messagingTemplate.convertAndSend(topicForLobby(s.getLobbyId()), evt);

        // Ende planen
        scheduleEndAt(s.getId(), endsAt);
    }

    @org.springframework.transaction.annotation.Transactional
    public void safeEndCurrentQuestion(UUID sessionId) {
        synchronized (lock(sessionId)) {
            endCurrentQuestion(sessionId, /*early*/ false);
        }
    }

    @Transactional
    protected void endCurrentQuestion(UUID sessionId, boolean earlyAdvance) {
        LiveQuizSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));

        if (s.getStatus() != SessionStatus.RUNNING) return;

        int idx = s.getCurrentIndex();

        // korrekte Antwort ermitteln
        SessionQuestion sq = sessionQuestionRepo.findBySession_IdAndIndexInSession(s.getId(), idx)
                .orElseThrow(() -> new EntityNotFoundException("SessionQuestion not found"));
        UUID correctAnswerId = sq.getQuestion().getAnswers().stream()
                .filter(Answer::getIsCorrect)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Question without correct answer"))
                .getId();

        // optional Leaderboard
        var leaderboard = participantRepo.getLeaderboard(s.getId()).stream()
                .map(r -> new LiveEvents.QuestionEnd.LeaderboardRow(r.getUserId(), r.getFirstName(), r.getName(), r.getScore()))
                .collect(Collectors.toList());

        var evt = LiveEvents.QuestionEnd.builder()
                .sessionId(s.getId())
                .index(idx)
                .correctAnswerId(correctAnswerId)
                .leaderboard(leaderboard)
                .build();
        messagingTemplate.convertAndSend(topicForLobby(s.getLobbyId()), evt);

        // Nächste Frage oder Finish
        if (s.hasMoreQuestions()) {
            Instant when = Instant.now().plus(s.getBufferDurationSec(), ChronoUnit.SECONDS);
            sessionRepo.save(s); // keep updatedAt
            scheduleNextQuestionAt(s.getId(), when, idx + 1);
        } else {
            finishSessionInternal(s);
        }

        // End-Future aufräumen (falls earlyAdvance)
        cancelFuture(scheduledEnds.remove(sessionId));
    }

    @Transactional
    protected void finishSessionInternal(LiveQuizSession s) {
        s.setStatus(SessionStatus.FINISHED);
        sessionRepo.save(s);

        var leaderboard = participantRepo.getLeaderboard(s.getId()).stream()
                .map(r -> new LiveEvents.QuestionEnd.LeaderboardRow(r.getUserId(), r.getFirstName(), r.getName(), r.getScore()))
                .collect(Collectors.toList());

        var evt = LiveEvents.QuizEnded.builder()
                .sessionId(s.getId())
                .leaderboard(leaderboard)
                .build();
        messagingTemplate.convertAndSend(topicForLobby(s.getLobbyId()), evt);

        // geplante Tasks aufräumen
        cancelFuture(scheduledEnds.remove(s.getId()));
        cancelFuture(scheduledNext.remove(s.getId()));
    }

    // -------------------- Antworten (REST) --------------------

    /**
     * Antwortet auf die aktuelle Frage.
     * - prüft Teilnahme, Zeitfenster, Idempotenz
     * - speichert ParticipantAnswer
     * - increment Score wenn korrekt
     * - Early-Advance wenn alle geantwortet haben
     */
    @Transactional
    public void submitAnswer(UUID sessionId, User user, int questionIndex, UUID answerId) {
        synchronized (lock(sessionId)) {
            LiveQuizSession s = sessionRepo.findById(sessionId)
                    .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));

            if (s.getStatus() != SessionStatus.RUNNING) {
                throw new IllegalStateException("Session not running.");
            }
            if (questionIndex != s.getCurrentIndex()) {
                throw new IllegalStateException("Answer is for wrong question index.");
            }
            if (Instant.now().isAfter(s.getQuestionEndsAt())) {
                throw new IllegalStateException("Too late. Question already ended.");
            }

            LiveQuizParticipant p = participantRepo.findBySession_IdAndUser_Id(s.getId(), user.getId())
                    .orElseThrow(() -> new SecurityException("Not a participant."));

            // idempotent: already answered?
            if (answerRepo.existsForParticipantAndIndex(p.getId(), questionIndex)) {
                return;
            }

            // Validierung: Antwort gehört zur richtigen Frage
            SessionQuestion sq = sessionQuestionRepo.findBySession_IdAndIndexInSession(s.getId(), questionIndex)
                    .orElseThrow(() -> new EntityNotFoundException("SessionQuestion not found"));
            boolean belongs = sq.getQuestion().getAnswers().stream()
                    .anyMatch(a -> a.getId().equals(answerId));
            if (!belongs) {
                throw new IllegalArgumentException("Answer does not belong to this question.");
            }

            Answer chosen = sq.getQuestion().getAnswers().stream()
                    .filter(a -> a.getId().equals(answerId))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Answer not found"));

            boolean correct = Boolean.TRUE.equals(chosen.getIsCorrect());

            ParticipantAnswer pa = ParticipantAnswer.builder()
                    .sessionId(s.getId())
                    .participant(p)
                    .questionIndex(questionIndex)
                    .answer(chosen)
                    .isCorrect(correct)
                    .answeredAt(java.time.LocalDateTime.now())
                    .build();
            answerRepo.save(pa);

            if (correct) {
                participantRepo.incrementScore(p.getId(), 1);
            }

            // Early-Advance?
            if (s.isRunning() && s.isEarlyAdvanceEnabled()) {
                long answered = answerRepo.countAnsweredForQuestion(s.getId(), questionIndex);
                long total = participantRepo.countBySession_Id(s.getId());
                if (answered == total) {
                    // end now and schedule next (buffer)
                    tx.endCurrentQuestion(s.getId(), true);
                    if (s.hasMoreQuestions()) {
                        Instant when = Instant.now().plusSeconds(s.getBufferDurationSec());
                        scheduleNextQuestionAt(s.getId(), when, questionIndex + 1);
                    } else {
                        // finished; runner will broadcast QUIZ_ENDED
                        cancelFuture(scheduledEnds.remove(s.getId()));
                        cancelFuture(scheduledNext.remove(s.getId()));
                    }
                } else {
                    // still waiting—ensure end is scheduled
                    scheduleEndAt(s.getId(), s.getQuestionEndsAt());
                }
            }
        }
    }

    // -------------------- Abbruch/Cancel --------------------

    @Transactional
    public void abortSessionByHostDisconnect(UUID lobbyId) {
        Optional<LiveQuizSession> opt = sessionRepo.findByLobbyId(lobbyId);
        if (opt.isEmpty()) return;
        LiveQuizSession s = opt.get();
        if (s.getStatus() == SessionStatus.CANCELLED || s.getStatus() == SessionStatus.FINISHED) return;

        s.setStatus(SessionStatus.CANCELLED);
        sessionRepo.save(s);

        var evt = LiveEvents.QuizAborted.builder()
                .sessionId(s.getId())
                .reason("HOST_DISCONNECTED")
                .build();
        messagingTemplate.convertAndSend(topicForLobby(s.getLobbyId()), evt);

        cancelFuture(scheduledEnds.remove(s.getId()));
        cancelFuture(scheduledNext.remove(s.getId()));
    }

    // -------------------- Snapshot (für Reconnect) --------------------

    @Transactional(readOnly = true)
    public Map<String, Object> getSessionState(UUID sessionId, User user) {
        LiveQuizSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found: " + sessionId));

        var meOpt = participantRepo.findBySession_IdAndUser_Id(sessionId, user.getId());
        if (meOpt.isEmpty()) throw new SecurityException("Not a participant.");

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", s.getStatus().name());
        out.put("currentIndex", s.getCurrentIndex());
        out.put("totalQuestions", s.getTotalQuestions());
        out.put("startAt", s.getStartAt());
        out.put("endsAt", s.getQuestionEndsAt());

        // aktuelle Frage (falls RUNNING)
        if (s.getStatus() == SessionStatus.RUNNING && s.getCurrentIndex() >= 0) {
            SessionQuestion sq = sessionQuestionRepo.findBySession_IdAndIndexInSession(s.getId(), s.getCurrentIndex())
                    .orElseThrow(() -> new EntityNotFoundException("SessionQuestion not found"));

            List<UUID> orderIds;
            try {
                orderIds = Arrays.asList(objectMapper.readValue(sq.getAnswerOrderJson(), UUID[].class));
            } catch (JsonProcessingException e) {
                orderIds = Collections.emptyList();
            }
            Map<String, Object> q = new LinkedHashMap<>();
            q.put("id", sq.getQuestion().getId());
            q.put("text", sq.getQuestion().getQuestionText());
            q.put("answers", orderIds.stream().map(aid -> {
                var a = sq.getQuestion().getAnswers().stream().filter(x -> x.getId().equals(aid)).findFirst().orElse(null);
                return Map.of("id", aid, "text", a != null ? a.getText() : "");
            }).collect(Collectors.toList()));
            out.put("question", q);
        }

        // eigener Status
        var me = meOpt.get();
        boolean answered = answerRepo.existsForParticipantAndIndex(me.getId(), s.getCurrentIndex());
        out.put("you", Map.of(
                "score", me.getScore(),
                "answered", answered
        ));

        return out;
    }
}
