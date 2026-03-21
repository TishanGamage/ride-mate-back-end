package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.resources.AvailableRideResponse;
import com.ride.mate.resources.RideRequestResource;
import com.ride.mate.resources.RideRequestResponse;
import com.ride.mate.service.RideRequestService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Ride Request Controller
 * REST API endpoints for the ride request/accept/reject flow
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Tishan           Initial Development
 */
@Slf4j
@RestController
@RequestMapping(value = "/ride-requests")
@CrossOrigin(origins = "*")
public class RideRequestController extends MessagePropertyBase {

    private final RideRequestService rideRequestService;

    public RideRequestController(RideRequestService rideRequestService) {
        this.rideRequestService = rideRequestService;
    }

    /**
     * Get all available rides for a passenger.
     * Filters by:
     * 1. Passenger destination is within radiusKm of the driver's end point.
     * 2. Passenger pickup point lies within the driver's route corridor.
     *
     * @param startLat  Passenger pickup latitude (optional)
     * @param startLng  Passenger pickup longitude (optional)
     * @param endLat    Passenger destination latitude (optional)
     * @param endLng    Passenger destination longitude (optional)
     * @param radiusKm  Search radius in km (optional, default 15)
     * @return List of available rides
     */
    @GetMapping("/available-rides")
    public ResponseEntity<List<AvailableRideResponse>> getAvailableRides(
            @RequestParam(value = "startLat", required = false) BigDecimal startLat,
            @RequestParam(value = "startLng", required = false) BigDecimal startLng,
            @RequestParam(value = "endLat", required = false) BigDecimal endLat,
            @RequestParam(value = "endLng", required = false) BigDecimal endLng,
            @RequestParam(value = "radiusKm", required = false) BigDecimal radiusKm) {

        log.info("GET /ride-requests/available-rides?startLat={}&startLng={}&endLat={}&endLng={}&radiusKm={}",
                startLat, startLng, endLat, endLng, radiusKm);

        List<AvailableRideResponse> rides = rideRequestService.getAvailableRides(startLat, startLng, endLat, endLng, radiusKm);
        return new ResponseEntity<>(rides, HttpStatus.OK);
    }

    /**
     * Create a ride request (passenger requests to join a ride).
     *
     * @param resource Ride request details
     * @return Created ride request response
     */
    @PostMapping
    public ResponseEntity<RideRequestResponse> createRideRequest(
            @Valid @RequestBody RideRequestResource resource) {

        log.info("POST /ride-requests — ride: {}, user: {}", resource.getRideDetailId(), resource.getUserId());

        RideRequestResponse response = rideRequestService.createRideRequest(resource);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all pending requests for a driver's active rides.
     *
     * @param driverProfileId Driver profile ID
     * @return List of pending ride requests with passenger details
     */
    @GetMapping("/driver/{driverProfileId}/pending")
    public ResponseEntity<List<RideRequestResponse>> getPendingRequestsForDriver(
            @PathVariable Long driverProfileId) {

        log.info("GET /ride-requests/driver/{}/pending", driverProfileId);

        List<RideRequestResponse> requests = rideRequestService.getPendingRequestsForDriver(driverProfileId);
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

    /**
     * Accept a ride request — passenger officially joins the ride.
     *
     * @param id Ride request ID
     * @return Updated ride request response
     */
    @PutMapping("/{id}/accept")
    public ResponseEntity<RideRequestResponse> acceptRideRequest(@PathVariable Long id) {

        log.info("PUT /ride-requests/{}/accept", id);

        RideRequestResponse response = rideRequestService.acceptRideRequest(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Reject a ride request.
     *
     * @param id Ride request ID
     * @return Updated ride request response
     */
    @PutMapping("/{id}/reject")
    public ResponseEntity<RideRequestResponse> rejectRideRequest(@PathVariable Long id) {

        log.info("PUT /ride-requests/{}/reject", id);

        RideRequestResponse response = rideRequestService.rejectRideRequest(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get all ride requests for a passenger.
     *
     * @param userId User ID
     * @return List of ride requests with their status
     */
    @GetMapping("/passenger/{userId}")
    public ResponseEntity<List<RideRequestResponse>> getRequestsByPassenger(@PathVariable Long userId) {

        log.info("GET /ride-requests/passenger/{}", userId);

        List<RideRequestResponse> requests = rideRequestService.getRequestsByPassenger(userId);
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }
}

