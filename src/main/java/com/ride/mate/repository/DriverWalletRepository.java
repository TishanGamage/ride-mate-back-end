package com.ride.mate.repository;

import com.ride.mate.domain.DriverWallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * DriverWalletRepository
 * Data access layer for DriverWallet entity
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Tishan          Initial Development
 */
@Repository
public interface DriverWalletRepository extends JpaRepository<DriverWallet, Long> {

    Optional<DriverWallet> findByDriverProfileId(Long driverProfileId);

    boolean existsByDriverProfileId(Long driverProfileId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM DriverWallet w WHERE w.driverProfile.id = :driverProfileId")
    Optional<DriverWallet> findByDriverProfileIdForUpdate(@Param("driverProfileId") Long driverProfileId);
}

