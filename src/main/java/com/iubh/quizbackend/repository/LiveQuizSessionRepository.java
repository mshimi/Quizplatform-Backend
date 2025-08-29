package com.iubh.quizbackend.repository;

import com.iubh.quizbackend.entity.quiz.LiveQuizSession;
import com.iubh.quizbackend.entity.quiz.SessionStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LiveQuizSessionRepository extends JpaRepository<LiveQuizSession, UUID> {

    Optional<LiveQuizSession> findByLobbyIdAndStatusIn(UUID lobbyId, Iterable<SessionStatus> statuses);

    Optional<LiveQuizSession> findByLobbyId(UUID lobbyId);

    boolean existsByLobbyIdAndStatusIn(UUID lobbyId, Iterable<SessionStatus> statuses);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update LiveQuizSession s
              set s.currentIndex = :newIndex,
                  s.questionEndsAt = :endsAt
            where s.id = :sessionId
           """)
    int updateIndexAndEndsAt(@Param("sessionId") UUID sessionId,
                             @Param("newIndex") int newIndex,
                             @Param("endsAt") java.time.Instant endsAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update LiveQuizSession s
              set s.status = :status
            where s.id = :sessionId
           """)
    int updateStatus(@Param("sessionId") UUID sessionId,
                     @Param("status") SessionStatus status);
}
