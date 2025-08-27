package com.iubh.quizbackend.repository;

import com.iubh.quizbackend.entity.quiz.QuizLobby;
import com.iubh.quizbackend.entity.quiz.QuizLobbyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizLobbyRepository extends JpaRepository<QuizLobby, UUID> {
    List<QuizLobby> findByStatus(QuizLobbyStatus status);

    @Transactional
    @Modifying
    @Query("UPDATE QuizLobby ql SET ql.status = 'CANCELLED' WHERE ql.host.id = :hostId AND ql.status = 'WAITING'")
    void cancelWaitingLobbiesByHostId(@Param("hostId") UUID hostId);

}
