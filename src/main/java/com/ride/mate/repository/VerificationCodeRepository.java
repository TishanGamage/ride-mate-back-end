package com.ride.mate.repository;

import com.ride.mate.domain.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Verification Code Repository
 * Data access layer for verification code operations
 *
 * @author Tishan 
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-02-2026    N/A          N/A          Tishan          Initial Development
 */
@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findByEmail(String email);

    Optional<VerificationCode> findByEmailAndCode(String email, String code);

    void deleteByExpiryTimeBefore(LocalDateTime dateTime);

    void deleteByEmail(String email);
}

