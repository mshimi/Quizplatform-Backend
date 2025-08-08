package com.iubh.quizbackend.repository;

import com.iubh.quizbackend.entity.quiz.Quiz;
import com.iubh.quizbackend.entity.quiz.QuizStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID> {


    Page<Quiz> findByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Quiz> findByUser_IdAndStatusOrderByCreatedAtDesc(UUID userId, QuizStatus status, Pageable pageable);

}