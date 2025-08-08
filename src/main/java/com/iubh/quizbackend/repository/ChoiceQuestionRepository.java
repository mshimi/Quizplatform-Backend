package com.iubh.quizbackend.repository;

import com.iubh.quizbackend.entity.question.ChoiceQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}