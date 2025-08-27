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

//    /**
//     * Finds a paginated list of questions belonging to a specific module.
//     *
//     * @param moduleId The ID of the module.
//     * @param pageable Pagination information (page, size, sort).
//     * @return A page of ChoiceQuestion entities.
//     */
//    Page<ChoiceQuestion> findByModule_Id(UUID moduleId, Pageable pageable);
//
//
//    /**
//     * Ruft eine begrenzte Anzahl zufälliger Fragen für ein bestimmtes Modul ab.
//     * Verwendet 'ORDER BY RANDOM()' für PostgreSQL, um eine effiziente Zufallsauswahl zu gewährleisten.
//     * @param moduleId Die ID des Moduls.
//     * @param limit Die maximale Anzahl der zurückzugebenden Fragen.
//     * @return Eine Liste von zufälligen ChoiceQuestion-Entitäten.
//     */
//    @Query(value = "SELECT * FROM choice_questions WHERE module_id = :moduleId ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
//    List<ChoiceQuestion> findRandomQuestionsByModule(@Param("moduleId") UUID moduleId, @Param("limit") int limit);
//
//
//    Page<ChoiceQuestion> findByQuestionTextContainingIgnoreCase(String searchText, Pageable pageable);
//
//    Page<ChoiceQuestion> findByModule_IdAndQuestionTextContainingIgnoreCase(UUID moduleId, String searchText, Pageable pageable);


    /**
     * Overrides the default findAll to only return active questions.
     */
    @Override
    @Query("SELECT q FROM ChoiceQuestion q WHERE q.active = true")
    Page<ChoiceQuestion> findAll(Pageable pageable);

    /**
     * Finds a paginated list of active questions belonging to a specific module.
     */
    Page<ChoiceQuestion> findByModule_IdAndActiveTrue(UUID moduleId, Pageable pageable);

    /**
     * Retrieves a limited number of random, active questions for a specific module.
     * Updated the native query to include 'AND active = true'.
     */
    @Query(value = "SELECT * FROM choice_questions WHERE module_id = :moduleId AND active = true ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<ChoiceQuestion> findRandomQuestionsByModule(@Param("moduleId") UUID moduleId, @Param("limit") int limit);

    /**
     * Finds a paginated list of active questions within a module where the question text contains the search term.
     */
    Page<ChoiceQuestion> findByModule_IdAndQuestionTextContainingIgnoreCaseAndActiveTrue(UUID moduleId, String searchText, Pageable pageable);

    /**
     * Finds a paginated list of active questions where the question text contains the search term.
     */
    Page<ChoiceQuestion> findByQuestionTextContainingIgnoreCaseAndActiveTrue(String searchText, Pageable pageable);


}