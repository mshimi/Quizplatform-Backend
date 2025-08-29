package com.iubh.quizbackend.repository;


import com.iubh.quizbackend.entity.quiz.ParticipantAnswer;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParticipantAnswerRepository extends JpaRepository<ParticipantAnswer, UUID> {

    // Alle Antworten eines Teilnehmers
    List<ParticipantAnswer> findByParticipant_IdOrderByQuestionIndexAsc(UUID participantId);

    // Antwort eines Teilnehmers zu einer bestimmten Frage (idempotent check)
    Optional<ParticipantAnswer> findByParticipant_IdAndQuestionIndex(UUID participantId, int questionIndex);

    // Wie viele Antworten gibt es für Frage X in Session?
    @Query("""
           select count(pa.id)
             from ParticipantAnswer pa
            where pa.sessionId = :sessionId
              and pa.questionIndex = :questionIndex
           """)
    long countAnsweredForQuestion(@Param("sessionId") UUID sessionId,
                                  @Param("questionIndex") int questionIndex);

    // Prüfen, ob ein User bereits für Frage X geantwortet hat (Shortcut)
    @Query("""
           select (count(pa.id) > 0)
             from ParticipantAnswer pa
            where pa.participant.id = :participantId
              and pa.questionIndex = :questionIndex
           """)
    boolean existsForParticipantAndIndex(@Param("participantId") UUID participantId,
                                         @Param("questionIndex") int questionIndex);



    @Query("""
       select (count(pa.id) > 0)
         from ParticipantAnswer pa
        where pa.sessionId = :sessionId
          and pa.questionIndex = :questionIndex
          and pa.participant.user.id = :userId
       """)
    boolean hasUserAnswered(@Param("sessionId") UUID sessionId,
                            @Param("questionIndex") int questionIndex,
                            @Param("userId") UUID userId);

}
