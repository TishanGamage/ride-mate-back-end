package com.ride.mate.repository;

import com.ride.mate.domain.User;
import com.ride.mate.enums.UserRole;
import com.ride.mate.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Repository
 * Data access layer for user authentication and account management
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 22-02-2026    N/A          N/A          Tishan          Initial Development
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    List<User> findAllByOrderByCreatedDateDesc();

    List<User> findByUserRoleOrderByCreatedDateDesc(UserRole userRole);

    long countByStatus(com.ride.mate.enums.UserStatus status);

    long countByUserRole(UserRole userRole);

    List<User> findByStatusOrderByCreatedDateDesc(UserStatus status);
}
