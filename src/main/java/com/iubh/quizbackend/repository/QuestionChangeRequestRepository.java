package com.iubh.quizbackend.repository;

import com.iubh.quizbackend.entity.change.QuestionChangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for managing QuestionChangeRequest entities and their subclasses.
 */
@Repository
public interface QuestionChangeRequestRepository extends JpaRepository<QuestionChangeRequest, UUID> {
}