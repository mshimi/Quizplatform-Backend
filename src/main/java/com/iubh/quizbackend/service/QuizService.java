package com.iubh.quizbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.iubh.quizbackend.api.dto.QuizResultDto;
import com.iubh.quizbackend.api.dto.QuizSummaryDto;
import com.iubh.quizbackend.api.dto.SubmitAnswerRequestDto;
import com.iubh.quizbackend.entity.module.Module;
import com.iubh.quizbackend.entity.question.Answer;
import com.iubh.quizbackend.entity.question.ChoiceQuestion;
import com.iubh.quizbackend.entity.quiz.Quiz;
import com.iubh.quizbackend.entity.quiz.QuizItem;
import com.iubh.quizbackend.entity.quiz.QuizStatus;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.mapper.QuizMapper;
import com.iubh.quizbackend.repository.AnswerRepository;
import com.iubh.quizbackend.repository.ChoiceQuestionRepository;
import com.iubh.quizbackend.repository.ModuleRepository;
import com.iubh.quizbackend.repository.QuizRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;


import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;
    private static final int QUIZ_QUESTION_COUNT = 10;
    private final ModuleRepository moduleRepository;
    private final ChoiceQuestionRepository choiceQuestionRepository;
    private final AnswerRepository answerRepository;


    private final ObjectMapper objectMapper;


    @Transactional
    public Quiz startQuiz(User currentUser, UUID moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Module not found with id: " + moduleId));

        List<ChoiceQuestion> randomQuestions = choiceQuestionRepository.findRandomQuestionsByModule(moduleId, QUIZ_QUESTION_COUNT);
        if (randomQuestions.isEmpty()) {
            throw new IllegalStateException("Not enough questions in the module to start a quiz.");
        }

        Quiz newQuiz = Quiz.builder()
                .user(currentUser)
                .module(module)
                .status(QuizStatus.IN_PROGRESS)
                .build();

        int order = 0;
        for (ChoiceQuestion question : randomQuestions) {
            QuizItem quizItem = QuizItem.builder().question(question).questionOrder(order++).build();

            // Shuffle answers and store their new order as a JSON string
            List<Answer> answers = new ArrayList<>(question.getAnswers());
            Collections.shuffle(answers);
            List<UUID> shuffledIds = answers.stream().map(Answer::getId).collect(Collectors.toList());

            try {
                quizItem.setShuffledAnswerOrder(objectMapper.writeValueAsString(shuffledIds));
            } catch (JsonProcessingException e) {
                // This would be a critical internal server error
                throw new RuntimeException("Could not serialize shuffled answer order.", e);
            }
            newQuiz.addQuizItem(quizItem);
        }
        return quizRepository.save(newQuiz);
    }

    /**
     * Retrieves a paginated history of quizzes for the given user.
     *
     * @param currentUser The user whose quiz history to fetch.
     * @param pageable    Pagination information.
     * @return A paginated list of quiz summaries.
     */
    @Transactional(readOnly = true)
    public Page<QuizSummaryDto> getQuizzesForUser(User currentUser, Pageable pageable, QuizStatus status) {
        Page<Quiz> quizPage;
        if (status == null) {
            quizPage = quizRepository.findByUser_IdOrderByCreatedAtDesc(currentUser.getId(), pageable);
        } else {
            quizPage = quizRepository.findByUser_IdAndStatusOrderByCreatedAtDesc(currentUser.getId(), status, pageable);
        }
        return quizPage.map(quizMapper::toSummaryDto);
    }


    /**
     * Retrieves a quiz by its ID, ensuring the requesting user is the owner.
     * The returned object's type depends on the quiz's status.
     *
     * @param quizId The ID of the quiz to retrieve.
     * @param currentUser The currently authenticated user.
     * @return An Object which is either a Quiz (for IN_PROGRESS) or a QuizResultDto (for COMPLETED).
     */
    @Transactional(readOnly = true)
    public Object getQuizById(UUID quizId, User currentUser) throws AccessDeniedException {
        // 1. Fetch the quiz from the database
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found with id: " + quizId));

        // 2. Security Check: Ensure the user requesting the quiz is the one who owns it.
        if (!quiz.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to view this quiz.");
        }

        // 3. Return a different object based on the quiz status
        if (quiz.getStatus() == QuizStatus.COMPLETED) {
            // For a completed quiz, we will eventually return detailed results.
            return quizMapper.toResultDto(quiz);

        } else {
            // For a quiz in progress, we return the full Quiz entity,
            // which the mapper will convert to a secure DTO without correct answers.
            return quiz;
        }
    }


    @Transactional
    public void submitAnswer(UUID quizId, User currentUser, SubmitAnswerRequestDto dto) throws AccessDeniedException {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found: " + quizId));

        if (!quiz.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to access this quiz.");
        }
        if (quiz.getStatus() == QuizStatus.COMPLETED) {
            throw new IllegalStateException("This quiz is already completed.");
        }

        QuizItem quizItem = quiz.getQuizItems().stream()
                .filter(item -> item.getQuestion().getId().equals(dto.getQuestionId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Question not found in this quiz."));

        // 1. Fetch the single Answer entity based on the submitted ID
        Answer selectedAnswer = answerRepository.findById(dto.getSelectedAnswerId())
                .orElseThrow(() -> new EntityNotFoundException("Answer not found."));

        // Security check: ensure the submitted answer belongs to the actual question
        if (!selectedAnswer.getQuestion().getId().equals(quizItem.getQuestion().getId())) {
            throw new IllegalArgumentException("Submitted answer does not belong to the question.");
        }

        // 2. Update the quiz item with the single selected answer
        quizItem.setSelectedAnswer(selectedAnswer);
        quizItem.setAnsweredAt(LocalDateTime.now());

        // 3. Perform a simple boolean check for correctness
        quizItem.setIsCorrect(selectedAnswer.getIsCorrect());

        quizRepository.save(quiz); // Persist all changes
    }



    @Transactional
    public Quiz finishQuiz(UUID quizId, User currentUser) throws AccessDeniedException {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found: " + quizId));

        if (!quiz.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to modify this quiz.");
        }
        if (quiz.getStatus() == QuizStatus.COMPLETED) {
            // If already completed, just return it without changes.
            return quiz;
        }

        // Finalize the quiz
        quiz.setStatus(QuizStatus.COMPLETED);
        quiz.setCompletedAt(LocalDateTime.now());

        return quizRepository.save(quiz);
    }


}