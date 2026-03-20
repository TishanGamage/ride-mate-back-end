package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.ShareRideDetail;
import com.ride.mate.resources.*;
import com.ride.mate.service.ShareRideDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Share Ride Detail Controller
 * Handles REST API endpoints for shared ride pooling operations
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Iruni           Initial Development
 */
@Slf4j
@RestController
@RequestMapping(value = "/shared-ride")
@CrossOrigin(origins = "*")
@Validated
public class ShareRideDetailController extends MessagePropertyBase {

    private final ShareRideDetailService shareRideDetailService;
    private final Environment environment;

    public ShareRideDetailController(ShareRideDetailService shareRideDetailService,
                                     Environment environment) {
        this.shareRideDetailService = shareRideDetailService;
        this.environment = environment;
    }

    /**
     * Join a shared ride pool
     * POST /shared-ride/join
     *
     * @param request Join shared ride request
     * @return ResponseEntity with shared ride details
     */
    @PostMapping(value = "/join")
    public ResponseEntity<?> joinSharedRide(@Valid @RequestBody ShareRideDetailAddResource request) {
        log.info("Received request to join shared ride for user ID: {}", request.getUserId());

        ShareRideDetail shareRideDetail = shareRideDetailService.joinSharedRide(request);

        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(shareRideDetail.getId());
        response.setMessages(environment.getProperty(SHARED_RIDE_CREATED));

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Search for available ride pools near passenger location
     * GET /shared-ride/search?startLat={}&startLng={}&endLat={}&endLng={}
     *
     * @param startLat Passenger start latitude
     * @param startLng Passenger start longitude
     * @param endLat Passenger end latitude
     * @param endLng Passenger end longitude
     * @return List of available ride pools
     */
    @GetMapping(value = "/search")
    public ResponseEntity<?> searchNearbyRides(
            @RequestParam BigDecimal startLat,
            @RequestParam BigDecimal startLng,
            @RequestParam BigDecimal endLat,
            @RequestParam BigDecimal endLng) {
        log.info("Searching for nearby shared rides");

        List<SharedRidePoolResponse> pools = shareRideDetailService.searchNearbyRides(
                startLat, startLng, endLat, endLng);

        log.info("Found {} available ride pools", pools.size());
        return ResponseEntity.ok(pools);
    }

    /**
     * Get available ride pools with custom radius
     * GET /shared-ride/available?startLat={}&startLng={}&endLat={}&endLng={}&radius={}
     *
     * @param startLat Passenger start latitude
     * @param startLng Passenger start longitude
     * @param endLat Passenger end latitude
     * @param endLng Passenger end longitude
     * @param radius Search radius in kilometers
     * @return List of available ride pools
     */
    @GetMapping(value = "/available")
    public ResponseEntity<?> getAvailableRidePools(
            @RequestParam BigDecimal startLat,
            @RequestParam BigDecimal startLng,
            @RequestParam BigDecimal endLat,
            @RequestParam BigDecimal endLng,
            @RequestParam(defaultValue = "15") BigDecimal radius) {
        log.info("Getting available ride pools within {} km radius", radius);

        List<SharedRidePoolResponse> pools = shareRideDetailService.getAvailableRidePools(
                startLat, startLng, endLat, endLng, radius);

        log.info("Found {} available ride pools", pools.size());
        return ResponseEntity.ok(pools);
    }

    /**
     * Get all passengers in a shared ride
     * GET /shared-ride/passengers/{rideDetailId}
     *
     * @param rideDetailId Ride detail ID
     * @return List of passengers in the ride
     */
    @GetMapping(value = "/passengers/{rideDetailId}")
    public ResponseEntity<?> getRidePassengers(@PathVariable Long rideDetailId) {
        log.info("Fetching passengers for ride ID: {}", rideDetailId);

        List<ShareRideDetailResponse> passengers = shareRideDetailService.getRidePassengers(rideDetailId);

        log.info("Found {} passengers for ride ID: {}", passengers.size(), rideDetailId);
        return ResponseEntity.ok(passengers);
    }

    /**
     * Get passenger's shared ride history
     * GET /shared-ride/history/{userId}
     *
     * @param userId User ID
     * @return List of completed shared rides
     */
    @GetMapping(value = "/history/{userId}")
    public ResponseEntity<?> getPassengerRideHistory(@PathVariable Long userId) {
        log.info("Fetching ride history for user ID: {}", userId);

        List<ShareRideDetailResponse> history = shareRideDetailService.getPassengerRideHistory(userId);

        log.info("Found {} completed rides for user ID: {}", history.size(), userId);
        return ResponseEntity.ok(history);
    }

    /**
     * Get detailed shared ride information
     * GET /shared-ride/details/{shareRideDetailId}
     *
     * @param shareRideDetailId Shared ride detail ID
     * @return Detailed shared ride information
     */
    @GetMapping(value = "/details/{shareRideDetailId}")
    public ResponseEntity<?> getSharedRideDetails(@PathVariable Long shareRideDetailId) {
        log.info("Fetching detailed shared ride information for ID: {}", shareRideDetailId);

        ShareRideDetailResponse details = shareRideDetailService.getSharedRideDetails(shareRideDetailId);

        return ResponseEntity.ok(details);
    }

    /**
     * Confirm/accept a shared ride
     * PUT /shared-ride/{shareRideDetailId}/confirm
     *
     * @param shareRideDetailId Shared ride detail ID
     * @return Updated shared ride details
     */
    @PutMapping(value = "/{shareRideDetailId}/confirm")
    public ResponseEntity<?> confirmSharedRide(@PathVariable Long shareRideDetailId) {
        log.info("Confirming shared ride ID: {}", shareRideDetailId);

        ShareRideDetail updated = shareRideDetailService.updateShareRideStatus(
                shareRideDetailId, "CONFIRMED");

        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(updated.getId());
        response.setMessages(environment.getProperty(SHARED_RIDE_CONFIRMED));

        return ResponseEntity.ok(response);
    }

    /**
     * Cancel a shared ride
     * PUT /shared-ride/{shareRideDetailId}/cancel
     *
     * @param shareRideDetailId Shared ride detail ID
     * @return Updated shared ride details
     */
    @PutMapping(value = "/{shareRideDetailId}/cancel")
    public ResponseEntity<?> cancelSharedRide(@PathVariable Long shareRideDetailId) {
        log.info("Cancelling shared ride ID: {}", shareRideDetailId);

        ShareRideDetail updated = shareRideDetailService.cancelSharedRide(shareRideDetailId);

        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(updated.getId());
        response.setMessages(environment.getProperty(SHARED_RIDE_CANCELLED));

        return ResponseEntity.ok(response);
    }

    /**
     * Calculate pooled cost for a ride
     * GET /shared-ride/cost/{rideDetailId}
     *
     * @param rideDetailId Ride detail ID
     * @return Pooled cost per passenger
     */
    @GetMapping(value = "/cost/{rideDetailId}")
    public ResponseEntity<?> calculatePooledCost(@PathVariable Long rideDetailId) {
        log.info("Calculating pooled cost for ride ID: {}", rideDetailId);

        BigDecimal costPerPassenger = shareRideDetailService.calculatePooledCost(rideDetailId);

        Map<String, Object> response = new HashMap<>();
        response.put("costPerPassenger", costPerPassenger);

        return ResponseEntity.ok(response);
    }

    /**
     * Update shared ride execution status
     * PUT /shared-ride/{shareRideDetailId}/status
     *
     * @param shareRideDetailId Shared ride detail ID
     * @param request Status update request
     * @return Updated shared ride details
     */
    @PutMapping(value = "/{shareRideDetailId}/status")
    public ResponseEntity<?> updateRideStatus(
            @PathVariable Long shareRideDetailId,
            @Valid @RequestBody StatusUpdateRequest request) {
        log.info("Updating ride status to: {} for ID: {}", request.getStatus(), shareRideDetailId);

        ShareRideDetail updated = shareRideDetailService.updateShareRideStatus(
                shareRideDetailId, request.getStatus());

        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(updated.getId());
        response.setMessages(environment.getProperty(RECORD_UPDATED));

        return ResponseEntity.ok(response);
    }

    /**
     * Request shared ride with ML-based driver matching
     * POST /shared-ride/request-with-matching
     *
     * @param request Shared ride request
     * @return Created shared ride after matching
     */
    @PostMapping(value = "/request-with-matching")
    public ResponseEntity<?> requestSharedRideWithMatching(
            @Valid @RequestBody ShareRideDetailAddResource request) {
        log.info("Processing shared ride request with ML-based matching");

        ShareRideDetail shareRideDetail = shareRideDetailService.requestSharedRideWithMatching(request);

        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(shareRideDetail.getId());
        response.setMessages(environment.getProperty(SHARED_RIDE_CREATED));

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
