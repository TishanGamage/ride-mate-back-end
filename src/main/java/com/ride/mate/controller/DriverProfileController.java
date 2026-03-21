package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.DriverProfile;
import com.ride.mate.domain.DriverVehicleDetails;
import com.ride.mate.resources.DriverProfileRequestResource;
import com.ride.mate.resources.DriverProfileResponse;
import com.ride.mate.resources.DriverVehicleDetailsRequestResource;
import com.ride.mate.resources.DriverVehicleListResponse;
import com.ride.mate.resources.SuccessAndErrorDetailsResource;
import com.ride.mate.service.DriverProfileService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * DriverProfileController
 * REST API endpoints for driver profile management operations
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 16-03-2026    N/A          N/A          Tishan          Initial Development
 * 2 17-03-2026    N/A          N/A          Tishan          Added getDriverProfileByUserId endpoint
 */
@Slf4j
@RestController
@RequestMapping(value = "/driver-profile")
@CrossOrigin(origins = "*")
public class DriverProfileController extends MessagePropertyBase {

    private final DriverProfileService driverProfileService;
    private final Environment environment;

    public DriverProfileController(DriverProfileService driverProfileService, Environment environment) {
        this.driverProfileService = driverProfileService;
        this.environment = environment;
    }

    /**
     * Save or update driver profile for a user
     * Creates a new driver profile and vehicle details, or updates if one already exists
     *
     * @param userId  the ID of the user
     * @param request driver profile request containing license and vehicle details
     * @return ResponseEntity with saved driver profile ID and success message
     */
    @PostMapping(value = "/save/{userId}")
    public ResponseEntity<SuccessAndErrorDetailsResource> saveDriverProfile(
            @PathVariable Long userId,
            @Valid @RequestBody DriverProfileRequestResource request) {
        log.info("Received driver profile save request for user ID: {}", userId);
        DriverProfile driverProfile = driverProfileService.saveDriverProfile(userId, request);
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(driverProfile.getId());
        response.setMessages(environment.getProperty(RECORD_CREATED));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Add a new vehicle to an existing driver profile
     *
     * @param driverProfileId the ID of the driver profile
     * @param request         vehicle details request
     * @return ResponseEntity with saved vehicle ID and success message
     */
    @PostMapping(value = "/{driverProfileId}/vehicle")
    public ResponseEntity<SuccessAndErrorDetailsResource> addVehicle(
            @PathVariable Long driverProfileId,
            @Valid @RequestBody DriverVehicleDetailsRequestResource request) {
        log.info("Received add vehicle request for driver profile ID: {}", driverProfileId);
        DriverVehicleDetails vehicle = driverProfileService.addVehicle(driverProfileId, request);
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(vehicle.getId());
        response.setMessages(environment.getProperty(RECORD_CREATED));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Update an existing vehicle's details
     *
     * @param vehicleId the ID of the vehicle record
     * @param request   updated vehicle details
     * @return ResponseEntity with updated vehicle ID and success message
     */
    @PutMapping(value = "/vehicle/{vehicleId}")
    public ResponseEntity<SuccessAndErrorDetailsResource> updateVehicle(
            @PathVariable Long vehicleId,
            @Valid @RequestBody DriverVehicleDetailsRequestResource request) {
        log.info("Received update vehicle request for vehicle ID: {}", vehicleId);
        DriverVehicleDetails vehicle = driverProfileService.updateVehicle(vehicleId, request);
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(vehicle.getId());
        response.setMessages(environment.getProperty(RECORD_UPDATED));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get all vehicles for a driver profile
     * Response includes hasMultipleVehicles flag and totalVehicles count
     *
     * @param driverProfileId the ID of the driver profile
     * @return ResponseEntity with vehicle list response
     */
    @GetMapping(value = "/{driverProfileId}/vehicles")
    public ResponseEntity<?> getVehiclesByDriverProfileId(@PathVariable Long driverProfileId) {
        log.info("Received request to get vehicles for driver profile ID: {}", driverProfileId);
        DriverVehicleListResponse response = driverProfileService.getVehiclesByDriverProfileId(driverProfileId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get driver profile by user ID
     *
     * @param userId the ID of the user
     * @return ResponseEntity with full driver profile details including vehicles
     */
    @GetMapping(value = "/get-driver-profile/user/{userId}")
    public ResponseEntity<Object> getDriverProfileByUserId(@PathVariable Long userId) {
        log.info("Received request to get driver profile for user ID: {}", userId);
        DriverProfileResponse driverProfile = driverProfileService.getDriverProfileByUserId(userId);
        return new ResponseEntity<>(driverProfile, HttpStatus.OK);
    }
}

