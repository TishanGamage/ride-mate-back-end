package com.ride.mate.service;

import com.ride.mate.domain.DriverProfile;
import com.ride.mate.domain.DriverVehicleDetails;
import com.ride.mate.resources.DriverProfileRequestResource;
import com.ride.mate.resources.DriverProfileResponse;
import com.ride.mate.resources.DriverVehicleDetailsRequestResource;
import com.ride.mate.resources.DriverVehicleDetailsResponse;
import com.ride.mate.resources.DriverVehicleListResponse;

import java.util.List;

/**
 * DriverProfileService
 * Service interface for driver profile management operations
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 16-03-2026    N/A          N/A          Tishan          Initial Development
 * 2 17-03-2026    N/A          N/A          Tishan          Added getDriverProfileByUserId
 */
public interface DriverProfileService {

    /**
     * Save or update driver profile and vehicle details for a given user
     *
     * @param userId  the ID of the user
     * @param request driver profile request containing license and vehicle details
     * @return the saved DriverProfile entity
     */
    DriverProfile saveDriverProfile(Long userId, DriverProfileRequestResource request);

    /**
     * Retrieve a driver profile by user ID
     *
     * @param userId the ID of the user
     * @return DriverProfileResponse containing full driver profile details
     */
    DriverProfileResponse getDriverProfileByUserId(Long userId);

    /**
     * Add a new vehicle to an existing driver profile
     *
     * @param driverProfileId the ID of the driver profile
     * @param request         vehicle details request
     * @return the saved DriverVehicleDetails entity
     */
    DriverVehicleDetails addVehicle(Long driverProfileId, DriverVehicleDetailsRequestResource request);

    /**
     * Update an existing vehicle's details
     *
     * @param vehicleId the ID of the driver vehicle details record
     * @param request   updated vehicle details
     * @return the updated DriverVehicleDetails entity
     */
    DriverVehicleDetails updateVehicle(Long vehicleId, DriverVehicleDetailsRequestResource request);

    /**
     * Get all vehicles for a driver profile with a hasMultipleVehicles flag
     *
     * @param driverProfileId the ID of the driver profile
     * @return list of vehicles and whether the driver has multiple vehicles
     */
    DriverVehicleListResponse getVehiclesByDriverProfileId(Long driverProfileId);
}

