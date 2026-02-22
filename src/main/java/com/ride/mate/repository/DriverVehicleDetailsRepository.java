package com.ride.mate.repository;

import com.ride.mate.domain.DriverProfile;
import com.ride.mate.domain.DriverVehicleDetails;
import com.ride.mate.enums.YesNo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Driver Vehicle Details Repository
 * Data access layer for driver vehicle operations
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-02-2026    N/A          N/A          Tishan          Initial Development
 * 2 22-02-2026    N/A          N/A          Tishan          Updated for DriverProfile entity
 */
@Repository
public interface DriverVehicleDetailsRepository extends JpaRepository<DriverVehicleDetails, Long> {

    List<DriverVehicleDetails> findByDriverProfile(DriverProfile driverProfile);

    List<DriverVehicleDetails> findByDriverProfileId(Long driverProfileId);

    Optional<DriverVehicleDetails> findByDriverProfileIdAndIsPrimary(Long driverProfileId, YesNo isPrimary);

    Optional<DriverVehicleDetails> findByDriverProfileAndIsPrimary(DriverProfile driverProfile, YesNo isPrimary);

    Optional<DriverVehicleDetails> findByRegistrationNumber(String registrationNumber);

    List<DriverVehicleDetails> findByDriverProfileIdAndStatus(Long driverProfileId, String status);

    List<DriverVehicleDetails> findByDriverProfileIdAndIsActive(Long driverProfileId, YesNo isActive);

    List<DriverVehicleDetails> findByStatus(String status);

    boolean existsByRegistrationNumber(String registrationNumber);

    long countByDriverProfileId(Long driverProfileId);
}

