package com.iubh.quizbackend.repository.specification;

import com.iubh.quizbackend.entity.module.Module;
import com.iubh.quizbackend.entity.user.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Provides reusable Specification instances for querying Module entities dynamically.
 */
public class ModuleSpecifications {

    /**
     * Creates a specification to find modules where the title contains the given string (case-insensitive).
     */
    public static Specification<Module> titleContains(String title) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    /**
     * Creates a specification to find modules that are followed by the given user.
     */
    public static Specification<Module> isFollowedBy(UUID userId) {
        return (root, query, criteriaBuilder) -> {
            // Join the 'followers' collection of the Module entity
            Join<Module, User> followersJoin = root.join("followers");
            // Create a predicate to check if the user's ID matches
            return criteriaBuilder.equal(followersJoin.get("id"), userId);
        };
    }

    /**
     * Creates a specification to find modules that are NOT followed by the given user.
     * This uses a subquery for efficiency and correctness.
     */
    public static Specification<Module> isNotFollowedBy(UUID userId) {
        return (root, query, criteriaBuilder) -> {
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<Module> subRoot = subquery.from(Module.class);
            Join<Module, User> subFollowers = subRoot.join("followers");
            subquery.select(subRoot.get("id")).where(criteriaBuilder.equal(subFollowers.get("id"), userId));

            // The main predicate is to find modules whose ID is NOT IN the subquery result.
            return criteriaBuilder.not(root.get("id").in(subquery));
        };
    }
}