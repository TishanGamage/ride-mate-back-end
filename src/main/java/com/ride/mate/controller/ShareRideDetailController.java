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
import java.util.List;

/**
 * Share Ride Detail Controller
 * Handles REST API endpoints for shared ride pooling operations.
 *
 * This controller owns ride discovery — GET /shared-ride/available calls the ML service
 * to rank active rides by driver acceptance probability before returning them to the passenger.
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Iruni           Initial Development
 * 2 21-03-2026    N/A          N/A          Tishan           Removed /search and /request-with-matching duplicates;
 *                                                            /available now accepts passengerRideDistance for accurate cost
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

    // ═══════════════════════════════════════════════════════════════════
    //  STEP 2 — Passenger discovers available rides (ML-ranked)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Get available rides for a passenger, ranked by ML acceptance probability.
     *
     * Flow:
     *   1. Fetch all ACTIVE rides within the given radius of the passenger's start point
     *   2. Call ML service to rank drivers by predicted acceptance rate
     *   3. Compute estimatedCostPerPassenger using max(60/N,20)% with the
     *      actual passengerRideDistance supplied by the caller
     *   4. Return list sorted best-match first
     *
     * GET /shared-ride/available?startLat={}&startLng={}&endLat={}&endLng={}
     *                            &passengerRideDistance={}&radius={}
     *
     * @param startLat              Passenger pickup latitude
     * @param startLng              Passenger pickup longitude
     * @param endLat                Passenger dropoff latitude
     * @param endLng                Passenger dropoff longitude
     * @param passengerRideDistance Passenger's route distance in km (used for accurate cost estimate)
     * @param radius                Search radius in km around passenger start (default 15)
     * @return ML-ranked list of available ride pools with per-passenger estimated cost
     */
    @GetMapping(value = "/available")
    public ResponseEntity<?> getAvailableRidePools(
            @RequestParam BigDecimal startLat,
            @RequestParam BigDecimal startLng,
            @RequestParam BigDecimal endLat,
            @RequestParam BigDecimal endLng,
            @RequestParam BigDecimal passengerRideDistance,
            @RequestParam(defaultValue = "15") BigDecimal radius) {

        log.info("GET /shared-ride/available — ({},{})→({},{}), dist={}km, radius={}km",
                startLat, startLng, endLat, endLng, passengerRideDistance, radius);

        List<SharedRidePoolResponse> pools = shareRideDetailService.getAvailableRidePools(
                startLat, startLng, endLat, endLng, passengerRideDistance, radius);

        log.info("Returning {} ML-ranked ride pools", pools.size());
        return ResponseEntity.ok(pools);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  STEP 3 — Passenger joins a ride (after driver accepts request)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Join a shared ride — creates a ShareRideDetail record for the passenger.
     * Called by the system after the driver accepts a ride request
     * (see PUT /ride-requests/{id}/accept which triggers cost recalculation).
     *
     * POST /shared-ride/join
     *
     * @param request Passenger location, ride ID, distance
     * @return Created ShareRideDetail ID and success message
     */
    @PostMapping(value = "/join")
    public ResponseEntity<?> joinSharedRide(@Valid @RequestBody ShareRideDetailAddResource request) {
        log.info("POST /shared-ride/join — user: {}, ride: {}", request.getUserId(), request.getRideDetailId());

        ShareRideDetail shareRideDetail = shareRideDetailService.joinSharedRide(request);

        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(shareRideDetail.getId());
        response.setMessages(environment.getProperty(SHARED_RIDE_CREATED));

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Ride info & history
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Get all passengers currently on a shared ride.
     * GET /shared-ride/passengers/{rideDetailId}
     */
    @GetMapping(value = "/passengers/{rideDetailId}")
    public ResponseEntity<?> getRidePassengers(@PathVariable Long rideDetailId) {
        log.info("GET /shared-ride/passengers/{}", rideDetailId);
        List<ShareRideDetailResponse> passengers = shareRideDetailService.getRidePassengers(rideDetailId);
        return ResponseEntity.ok(passengers);
    }

    /**
     * Get a passenger's completed ride history.
     * GET /shared-ride/history/{userId}
     */
    @GetMapping(value = "/history/{userId}")
    public ResponseEntity<?> getPassengerRideHistory(@PathVariable Long userId) {
        log.info("GET /shared-ride/history/{}", userId);
        List<ShareRideDetailResponse> history = shareRideDetailService.getPassengerRideHistory(userId);
        return ResponseEntity.ok(history);
    }

    /**
     * Get detailed info for a single shared-ride record.
     * GET /shared-ride/details/{shareRideDetailId}
     */
    @GetMapping(value = "/details/{shareRideDetailId}")
    public ResponseEntity<?> getSharedRideDetails(@PathVariable Long shareRideDetailId) {
        log.info("GET /shared-ride/details/{}", shareRideDetailId);
        ShareRideDetailResponse details = shareRideDetailService.getSharedRideDetails(shareRideDetailId);
        return ResponseEntity.ok(details);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Status management
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Mark a shared ride as CONFIRMED (driver on the way to pickup).
     * PUT /shared-ride/{shareRideDetailId}/confirm
     */
    @PutMapping(value = "/{shareRideDetailId}/confirm")
    public ResponseEntity<?> confirmSharedRide(@PathVariable Long shareRideDetailId) {
        log.info("PUT /shared-ride/{}/confirm", shareRideDetailId);

        ShareRideDetail updated = shareRideDetailService.updateShareRideStatus(shareRideDetailId, "CONFIRMED");

        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(updated.getId());
        response.setMessages(environment.getProperty(SHARED_RIDE_CONFIRMED));
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel a shared ride (passenger cancels after joining).
     * PUT /shared-ride/{shareRideDetailId}/cancel
     */
    @PutMapping(value = "/{shareRideDetailId}/cancel")
    public ResponseEntity<?> cancelSharedRide(@PathVariable Long shareRideDetailId) {
        log.info("PUT /shared-ride/{}/cancel", shareRideDetailId);

        ShareRideDetail updated = shareRideDetailService.cancelSharedRide(shareRideDetailId);

        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(updated.getId());
        response.setMessages(environment.getProperty(SHARED_RIDE_CANCELLED));
        return ResponseEntity.ok(response);
    }

    /**
     * Generic status update for a shared ride record.
     * PUT /shared-ride/{shareRideDetailId}/status
     */
    @PutMapping(value = "/{shareRideDetailId}/status")
    public ResponseEntity<?> updateRideStatus(
            @PathVariable Long shareRideDetailId,
            @Valid @RequestBody StatusUpdateRequest request) {
        log.info("PUT /shared-ride/{}/status → {}", shareRideDetailId, request.getStatus());

        ShareRideDetail updated = shareRideDetailService.updateShareRideStatus(
                shareRideDetailId, request.getStatus());

        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(updated.getId());
        response.setMessages(environment.getProperty(RECORD_UPDATED));
        return ResponseEntity.ok(response);
    }
}
