package com.ride.mate.repository;

import com.ride.mate.domain.WithdrawalRequest;
import com.ride.mate.enums.WithdrawalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * WithdrawalRequestRepository
 * Data access layer for WithdrawalRequest entity
 *
 * @author Danushka
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-03-2026    N/A          N/A          Danushka          Initial Development
 */
@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long> {

    List<WithdrawalRequest> findByDriverProfileId(Long driverProfileId);

    List<WithdrawalRequest> findByDriverProfileIdAndStatus(Long driverProfileId, WithdrawalStatus status);

    List<WithdrawalRequest> findByStatus(WithdrawalStatus status);
}

