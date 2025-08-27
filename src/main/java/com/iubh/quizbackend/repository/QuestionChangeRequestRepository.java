package com.iubh.quizbackend.repository;

import com.iubh.quizbackend.entity.change.ChangeRequestStatus;
import com.iubh.quizbackend.entity.change.QuestionChangeRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for managing QuestionChangeRequest entities and their subclasses.
 */
@Repository
public interface QuestionChangeRequestRepository extends JpaRepository<QuestionChangeRequest, UUID> {
    Page<QuestionChangeRequest> findByQuestionId(UUID questionId, Pageable pageable);

    Page<QuestionChangeRequest> findByQuestion_ModuleId(UUID moduleId, Pageable pageable);


    Page<QuestionChangeRequest> findByQuestion_ModuleIdInAndStatus(List<UUID> moduleIds, ChangeRequestStatus status, Pageable pageable);

}