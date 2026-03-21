package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.resources.AvailableRideResponse;
import com.ride.mate.resources.RideRequestResource;
import com.ride.mate.resources.RideRequestResponse;
import com.ride.mate.service.RideRequestService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Ride Request Controller
 * REST API endpoints for the ride request/accept/reject/cancel flow.
 *
 * Ride discovery (browsing available rides) is handled by ShareRideDetailController
 * which uses the ML service via GET /shared-ride/available.
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Tishan           Initial Development
 * 2 21-03-2026    N/A          N/A          Tishan           Added cancel and estimate-cost endpoints
 * 3 21-03-2026    N/A          N/A          Tishan           Removed getAvailableRides (use /shared-ride/available)
 */
@Slf4j
@RestController
@RequestMapping(value = "/ride-requests")
@CrossOrigin(origins = "*")
public class RideRequestController extends MessagePropertyBase {

    private final RideRequestService rideRequestService;

    public RideRequestController(RideRequestService rideRequestService,
                                  Environment environment) {
        this.rideRequestService = rideRequestService;
        this.environment = environment;
    }

    /**
     * Estimate the cost a passenger would pay before submitting a request.
     * Uses max(60/N, 20)% algorithm where N = current passengers + 1.
     *
     * @param rideDetailId          The target ride ID
     * @param passengerRideDistance The passenger's route distance in km
     * @return Estimated cost with share percentage and pricing note
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
    @GetMapping("/{rideDetailId}/estimate-cost")
    public ResponseEntity<PassengerEstimatedCostResponse> estimatePassengerCost(
            @PathVariable Long rideDetailId,
            @RequestParam("passengerRideDistance") BigDecimal passengerRideDistance) {

        log.info("GET /ride-requests/{}/estimate-cost?passengerRideDistance={}", rideDetailId, passengerRideDistance);

        PassengerEstimatedCostResponse response = rideRequestService.estimatePassengerCost(
                rideDetailId, passengerRideDistance);
        return new ResponseEntity<>(response, HttpStatus.OK);
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
     * Automatically recalculates cost split and returns the passenger's calculated cost.
     *
     * @param id Ride request ID
     * @return Updated ride request response with estimatedCost populated
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
     * Cancel a ride request (passenger withdraws).
     * Works for PENDING (before driver acts) and ACCEPTED (passenger already joined).
     * If ACCEPTED, removes from ride and cost is recalculated for remaining passengers.
     *
     * @param id Ride request ID
     * @return Updated ride request response with CANCELLED status
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<RideRequestResponse> cancelRideRequest(@PathVariable Long id) {

        log.info("PUT /ride-requests/{}/cancel", id);

        RideRequestResponse response = rideRequestService.cancelRideRequest(id);
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
