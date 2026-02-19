package com.ride.mate.repository;

import com.ride.mate.domain.DriverProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
 */
@Repository
public interface DriverProfileRepository extends JpaRepository<DriverProfile, Long> {

    Optional<DriverProfile> findByUserProfileId(Long userProfileId);

    Optional<DriverProfile> findByDriverLicenseNumber(String driverLicenseNumber);
}

