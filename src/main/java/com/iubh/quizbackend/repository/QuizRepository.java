package com.iubh.quizbackend.repository;

import com.iubh.quizbackend.entity.quiz.Quiz;
import com.iubh.quizbackend.entity.quiz.QuizStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID> {


    Page<Quiz> findByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<Quiz> findByUser_IdAndStatusOrderByCreatedAtDesc(UUID userId, QuizStatus status, Pageable pageable);


    @Query("SELECT q.module.id, q.module.title, q.completedAt, " +
           "SUM(CASE WHEN qi.isCorrect = TRUE THEN 1 ELSE 0 END), " +
           "COUNT(qi.id) " +
           "FROM Quiz q JOIN q.quizItems qi " +
           "WHERE q.user.id = :userId AND q.status = 'COMPLETED' AND q.completedAt >= :startDate " +
           "GROUP BY q.id, q.module.id, q.module.title, q.completedAt")
    List<Object[]> findStatisticsByUserIdAndDate(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate
    );

}