package com.ride.mate.service;

import com.ride.mate.domain.VehicleType;

import java.util.List;
import java.util.Optional;

/**
 * VehicleTypeService
 * Service interface for vehicle type business logic
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 25-02-2026    N/A          N/A          Iruni           Initial Development
 * 2 17-03-2026    N/A          N/A          Tishan          Updated to follow coding standards
 * 3 18-03-2026    N/A          N/A          Tishan          Added updatePerKmRates for scheduler
 */
public interface VehicleTypeService {

    /**
     * Get VehicleType by id
     *
     * @param id - Vehicle Type Id
     * @return Optional of VehicleType
     */
    Optional<VehicleType> findById(long id);

    /**
     * Get VehicleTypes by status
     *
     * @param status - status
     * @return List of VehicleType records matching the status
     */
    List<VehicleType> findByStatus(String status);

    /**
     * Update per km rates for all vehicle types based on time of day.
     * Applies night rates during night hours (20:00 - 05:59) and day rates otherwise.
     */
    void updatePerKmRates();
}
