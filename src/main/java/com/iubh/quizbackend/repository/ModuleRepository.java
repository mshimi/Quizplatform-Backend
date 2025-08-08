package com.iubh.quizbackend.repository;

import com.iubh.quizbackend.entity.module.Module;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModuleRepository extends JpaRepository<Module, UUID>, JpaSpecificationExecutor<Module> {
    // Spring Data JPA automatically provides CRUD methods.
    // You can add custom queries here if needed, for example:
    Optional<Module> findByTitle(String title);


    Page<Module> findByFollowers_Id(UUID userId, Pageable pageable);
}