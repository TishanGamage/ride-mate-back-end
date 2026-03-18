package com.ride.mate.repository;

import com.ride.mate.domain.DriverEarning;
import com.ride.mate.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * DriverEarningRepository
 * Data access layer for DriverEarning entity
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
public interface DriverEarningRepository extends JpaRepository<DriverEarning, Long> {

    List<DriverEarning> findByDriverProfileId(Long driverProfileId);

    List<DriverEarning> findByDriverProfileIdAndStatus(Long driverProfileId, PaymentStatus status);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM DriverEarning e WHERE e.driverProfile.id = :driverProfileId AND e.status = :status")
    BigDecimal sumAmountByDriverProfileIdAndStatus(@Param("driverProfileId") Long driverProfileId, @Param("status") PaymentStatus status);
}

