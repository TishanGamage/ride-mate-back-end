package com.ride.mate.repository;

import com.ride.mate.domain.User;
import com.ride.mate.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Profile Repository
 * Data access layer for user profile operations
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-02-2026    N/A          N/A          Tishan          Initial Development
 * 2 22-02-2026    N/A          N/A          Tishan          Updated for User-based schema
 * 3 22-03-2026    N/A          N/A          Dulan           Added JOIN FETCH query for eager document loading
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUser(User user);

    Optional<UserProfile> findByUserId(Long userId);

    /**
     * Find user profile by user ID with eagerly loaded document relationships
     * Prevents lazy loading issues when building response DTOs
     */
    @Query("SELECT up FROM UserProfile up " +
           "LEFT JOIN FETCH up.user " +
           "LEFT JOIN FETCH up.profileImageDocument " +
           "LEFT JOIN FETCH up.userVerificationImageDocument " +
           "WHERE up.user.id = :userId")
    Optional<UserProfile> findByUserIdWithDocuments(@Param("userId") Long userId);

    boolean existsByUserId(Long userId);
}

