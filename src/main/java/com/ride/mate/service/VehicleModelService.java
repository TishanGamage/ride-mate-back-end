package com.ride.mate.service;

import com.ride.mate.domain.VehicleModel;

import java.util.List;

/**
 * VehicleModelService
 * Service interface for vehicle model business logic
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-03-2026    N/A          N/A          Iruni           Initial Development
 */
public interface VehicleModelService {

    /**
     * Get VehicleModels by vehicle make id and status
     *
     * @param vehicleMakeId - Vehicle Make Id
     * @param status - Status
     * @return List of VehicleModel records matching the criteria
     */
    List<VehicleModel> findByVehicleMakeIdAndStatus(Long vehicleMakeId, String status);
}

