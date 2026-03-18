package com.ride.mate.repository;

import com.ride.mate.domain.VehicleTypeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * VehicleTypeRateRepository
 * Data access layer for vehicle type rate operations
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-03-2026    N/A          N/A          Tishan          Initial Development
 */
@Repository
public interface VehicleTypeRateRepository extends JpaRepository<VehicleTypeRate, Long> {

    /**
     * Find the active rate configuration for a specific vehicle type.
     *
     * @param vehicleTypeId the vehicle type ID
     * @param status the status (e.g., ACTIVE)
     * @return Optional of VehicleTypeRate
     */
    Optional<VehicleTypeRate> findByVehicleTypeIdAndStatus(Long vehicleTypeId, String status);
}

