package com.iubh.quizbackend.service;

import com.iubh.quizbackend.api.dto.QuizSummaryDto;
import com.iubh.quizbackend.entity.quiz.Quiz;
import com.iubh.quizbackend.entity.quiz.QuizStatus;
import com.iubh.quizbackend.entity.user.User;
import com.iubh.quizbackend.mapper.QuizMapper;
import com.iubh.quizbackend.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;

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
}