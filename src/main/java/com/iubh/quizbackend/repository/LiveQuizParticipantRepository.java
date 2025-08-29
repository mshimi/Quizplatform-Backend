package com.iubh.quizbackend.repository;



import com.iubh.quizbackend.entity.quiz.LiveQuizParticipant;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LiveQuizParticipantRepository extends JpaRepository<LiveQuizParticipant, UUID> {

    List<LiveQuizParticipant> findBySession_Id(UUID sessionId);

    Optional<LiveQuizParticipant> findBySession_IdAndUser_Id(UUID sessionId, UUID userId);

    long countBySession_Id(UUID sessionId);

    // Leaderboard: sortiert nach Score desc, optional später by name/tie-breaker
    List<LiveQuizParticipant> findBySession_IdOrderByScoreDesc(UUID sessionId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update LiveQuizParticipant p
              set p.connected = :connected,
                  p.lastSeenAt = CURRENT_TIMESTAMP
            where p.session.id = :sessionId
              and p.user.id = :userId
           """)
    int markConnection(@Param("sessionId") UUID sessionId,
                       @Param("userId") UUID userId,
                       @Param("connected") boolean connected);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update LiveQuizParticipant p
              set p.score = p.score + :delta
            where p.id = :participantId
           """)
    int incrementScore(@Param("participantId") UUID participantId,
                       @Param("delta") int delta);



    @Query("""
       select p.user.id as userId,
              p.user.profile.firstName as firstName,
              p.user.profile.name as name,
              p.score as score
         from LiveQuizParticipant p
        where p.session.id = :sessionId
        order by p.score desc
       """)
    List<com.iubh.quizbackend.repository.projection.LeaderboardRow> getLeaderboard(@Param("sessionId") UUID sessionId);

}
