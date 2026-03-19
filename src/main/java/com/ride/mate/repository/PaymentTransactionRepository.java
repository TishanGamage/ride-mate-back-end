package com.ride.mate.repository;

import com.ride.mate.domain.PaymentTransaction;
import com.ride.mate.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * PaymentTransactionRepository
 * Data access layer for PaymentTransaction entity
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
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByOrderId(String orderId);

    boolean existsByOrderId(String orderId);

    List<PaymentTransaction> findByUserId(Long userId);

    List<PaymentTransaction> findByRideDetailId(Long rideDetailId);

    List<PaymentTransaction> findByUserIdAndStatus(Long userId, PaymentStatus status);
}

