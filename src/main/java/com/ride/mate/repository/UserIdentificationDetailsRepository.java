package com.ride.mate.repository;

import com.ride.mate.domain.IdentificationType;
import com.ride.mate.domain.User;
import com.ride.mate.domain.UserIdentificationDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Identification Details Repository
 * Data access layer for user identification operations
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-02-2026    N/A          N/A          Tishan          Initial Development
 * 2 22-02-2026    N/A          N/A          Tishan          Updated for User-based schema
 */
@Repository
public interface UserIdentificationDetailsRepository extends JpaRepository<UserIdentificationDetails, Long> {

    List<UserIdentificationDetails> findByUser(User user);

    List<UserIdentificationDetails> findByUserId(Long userId);

    Optional<UserIdentificationDetails> findByUserAndIdentificationType(User user, IdentificationType identificationType);

    Optional<UserIdentificationDetails> findByUserIdAndIdentificationTypeId(Long userId, Long identificationTypeId);

    Optional<UserIdentificationDetails> findByIdentificationNumber(String identificationNumber);

    List<UserIdentificationDetails> findByUserIdAndStatus(Long userId, String status);

    boolean existsByUserIdAndIdentificationTypeId(Long userId, Long identificationTypeId);

    boolean existsByUserId(Long userId);
}

