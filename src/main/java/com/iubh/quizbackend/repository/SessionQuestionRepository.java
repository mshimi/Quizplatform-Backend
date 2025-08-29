package com.iubh.quizbackend.repository;


import com.iubh.quizbackend.entity.quiz.SessionQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionQuestionRepository extends JpaRepository<SessionQuestion, UUID> {

    List<SessionQuestion> findBySession_IdOrderByIndexInSessionAsc(UUID sessionId);

    Optional<SessionQuestion> findBySession_IdAndIndexInSession(UUID sessionId, int indexInSession);

    boolean existsBySession_IdAndIndexInSession(UUID sessionId, int indexInSession);

    long countBySession_Id(UUID sessionId);

    @Query("""
   select sq
     from SessionQuestion sq
     join fetch sq.question q
     left join fetch q.answers
    where sq.session.id = :sessionId
      and sq.indexInSession = :index
""")
    Optional<SessionQuestion> findWithQuestionAndAnswers(
            @Param("sessionId") UUID sessionId,
            @Param("index") int index);

}
