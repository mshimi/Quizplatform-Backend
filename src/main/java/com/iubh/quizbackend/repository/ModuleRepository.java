package com.iubh.quizbackend.repository;

import com.iubh.quizbackend.entity.module.Module;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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



    /**
     * Finds all modules that a specific user is following.
     * This is an efficient way to get the required data without loading the entire User object's collections.
     * @param userId The ID of the user.
     * @return A list of Modules followed by the user.
     */
    @Query("SELECT m FROM Module m JOIN m.followers u WHERE u.id = :userId")
    List<Module> findModulesFollowedByUserId(@Param("userId") UUID userId);
}