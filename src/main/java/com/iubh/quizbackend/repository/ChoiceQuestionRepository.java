package com.iubh.quizbackend.repository;

import com.iubh.quizbackend.entity.question.ChoiceQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChoiceQuestionRepository extends JpaRepository<ChoiceQuestion, UUID> {

    /**
     * Finds a paginated list of questions belonging to a specific module.
     *
     * @param moduleId The ID of the module.
     * @param pageable Pagination information (page, size, sort).
     * @return A page of ChoiceQuestion entities.
     */
    Page<ChoiceQuestion> findByModule_Id(UUID moduleId, Pageable pageable);


    /**
     * Ruft eine begrenzte Anzahl zufälliger Fragen für ein bestimmtes Modul ab.
     * Verwendet 'ORDER BY RANDOM()' für PostgreSQL, um eine effiziente Zufallsauswahl zu gewährleisten.
     * @param moduleId Die ID des Moduls.
     * @param limit Die maximale Anzahl der zurückzugebenden Fragen.
     * @return Eine Liste von zufälligen ChoiceQuestion-Entitäten.
     */
    @Query(value = "SELECT * FROM choice_questions WHERE module_id = :moduleId ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<ChoiceQuestion> findRandomQuestionsByModule(@Param("moduleId") UUID moduleId, @Param("limit") int limit);


}