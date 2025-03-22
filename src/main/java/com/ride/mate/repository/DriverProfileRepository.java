package com.ride.mate.repository;

import com.ride.mate.domain.DriverProfile;
import com.ride.mate.domain.User;
import com.ride.mate.enums.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Driver Profile Repository
 * Data access layer for driver profile operations
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
public interface DriverProfileRepository extends JpaRepository<DriverProfile, Long> {

    Optional<DriverProfile> findByUser(User user);

    Optional<DriverProfile> findByUserId(Long userId);

    Optional<DriverProfile> findByDriverLicenseNumber(String driverLicenseNumber);

    List<DriverProfile> findByAccountStatus(DriverStatus accountStatus);

    boolean existsByUserId(Long userId);

    boolean existsByDriverLicenseNumber(String driverLicenseNumber);

    long countByAccountStatus(DriverStatus accountStatus);
}
