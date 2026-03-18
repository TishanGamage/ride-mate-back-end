package com.ride.mate.service;

import com.ride.mate.domain.VehicleMake;

import java.util.List;
import java.util.Optional;

/**
 * VehicleMakeService
 * Service interface for vehicle make business logic
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 17-03-2026    N/A          N/A          Tishan          Initial Development
 */
public interface VehicleMakeService {

    /**
     * Get VehicleMake by id
     *
     * @param id - Vehicle Make Id
     * @return Optional of VehicleMake
     */
    Optional<VehicleMake> findById(long id);

    /**
     * Get VehicleMakes by status
     *
     * @param status - status
     * @return List of VehicleMake records matching the status
     */
    List<VehicleMake> findByStatus(String status);
}

