package com.iubh.quizbackend.repository;

import com.iubh.quizbackend.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.followedModules m WHERE u.id IN :userIds AND m.id = :moduleId")
    List<User> findActiveUsersFollowingModule(@Param("userIds") List<UUID> userIds, @Param("moduleId") UUID moduleId);




    /**
     * Finds a user by their ID and eagerly fetches their followedModules collection.
     * This prevents LazyInitializationException when the collection is accessed outside of a transaction.
     * @param userId The ID of the user to find.
     * @return An Optional containing the User with their followed modules initialized.
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.followedModules WHERE u.id = :userId")
    Optional<User> findByIdWithFollowedModules(@Param("userId") UUID userId);



}
