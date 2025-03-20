package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.RideDetail;
import com.ride.mate.resources.CostSplitResponse;
import com.ride.mate.resources.RideDetailResponseResource;
import com.ride.mate.resources.PassengerRideConfirmRequestResource;
import com.ride.mate.resources.RideDetailRequestResource;
import com.ride.mate.resources.RidePriceCalculationResponse;
import com.ride.mate.resources.SuccessAndErrorDetailsResource;
import com.ride.mate.service.CostSplitService;
import com.ride.mate.service.RideDetailService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * RideDetail Controller
 * REST API endpoints for RideDetails
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 15-03-2026    N/A          N/A          Iruni           Initial Development
 * 2 19-03-2026    N/A          N/A          Iruni           Added calculate ride price endpoint
 * 3 20-03-2026    N/A          N/A          Tishan           Added cost split and passenger confirm endpoints
 */

@Slf4j
@RestController
@RequestMapping(value = "/ride-details")
@CrossOrigin(origins = "*")
public class RideDetailController extends MessagePropertyBase {

    private final RideDetailService rideDetailService;
    private final CostSplitService costSplitService;
    private final Environment environment;

    public RideDetailController(RideDetailService rideDetailService,
                                CostSplitService costSplitService,
                                Environment environment) {
        this.rideDetailService = rideDetailService;
        this.costSplitService = costSplitService;
        this.environment = environment;
    }

    /**
     * Create a new ride detail
     *
     * @param request RideDetailRequestResource containing ride information
     * @return ResponseEntity with success response
     */
    @PostMapping("/addRide")
    public ResponseEntity<?> createRideDetail(@Valid @RequestBody RideDetailRequestResource request) {
        log.info("Received request to create ride detail for driver profile ID: {}", request.getDriverProfileId());

        RideDetail rideDetail = rideDetailService.createRideDetail(request);

        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(rideDetail.getId());
        response.setMessages(environment.getProperty(RECORD_CREATED));

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Calculate ride price based on total distance and driver profile
     * Algorithm: Get driver vehicle -> Get vehicle type -> Get per km rate -> Calculate price
     *
     * @param driverProfileId Driver profile ID
     * @param totalDistance Total distance in kilometers
     * @return ResponseEntity with calculated ride price details
     */
    @GetMapping("/calculate-price")
    public ResponseEntity<?> calculateRidePrice(
            @RequestParam("driverProfileId") Long driverProfileId,
            @RequestParam("totalDistance") BigDecimal totalDistance) {

        log.info("Received request to calculate ride price for driver profile ID: {} with distance: {} km",
                driverProfileId, totalDistance);

        RidePriceCalculationResponse response = rideDetailService.calculateRidePrice(driverProfileId, totalDistance);

        log.info("Ride price calculation successful: {}", response.getTotalRidePrice());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Confirm a passenger joining a ride.
     * After confirmation, the cost split is automatically recalculated.
     *
     * @param request PassengerRideConfirmRequestResource
     * @return ResponseEntity with cost split response
     */
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPassengerRide(
            @Valid @RequestBody PassengerRideConfirmRequestResource request) {

        log.info("Received passenger ride confirm request for ride ID: {}, user ID: {}",
                request.getRideDetailId(), request.getUserId());

        // Create the shared ride detail and recalculate cost split
        CostSplitResponse response = rideDetailService.confirmPassengerRide(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get cost split breakdown for a ride.
     * Returns segment-by-segment cost analysis showing how costs are distributed
     * among the driver and all passengers.
     *
     * @param rideDetailId The ride detail ID
     * @return ResponseEntity with CostSplitResponse
     */
    @GetMapping("/{rideDetailId}/cost-split")
    public ResponseEntity<?> getCostSplit(@PathVariable Long rideDetailId) {

        log.info("Received request to get cost split for ride ID: {}", rideDetailId);

        CostSplitResponse response = costSplitService.getCostSplit(rideDetailId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Recalculate cost split for a ride.
     * Useful when passengers join or leave mid-ride.
     *
     * @param rideDetailId The ride detail ID
     * @return ResponseEntity with updated CostSplitResponse
     */
    @PostMapping("/{rideDetailId}/cost-split/recalculate")
    public ResponseEntity<?> recalculateCostSplit(@PathVariable Long rideDetailId) {

        log.info("Received request to recalculate cost split for ride ID: {}", rideDetailId);

        CostSplitResponse response = costSplitService.calculateCostSplit(rideDetailId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * End an active ride by updating its status to COMPLETED
     *
     * @param rideDetailId The ride detail ID
     * @return ResponseEntity with success response
     */
    @PutMapping("/{rideDetailId}/end")
    public ResponseEntity<?> endRide(@PathVariable Long rideDetailId) {
        log.info("Received request to end ride ID: {}", rideDetailId);

        RideDetail rideDetail = rideDetailService.endRide(rideDetailId);

        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(rideDetail.getId());
        response.setMessages(environment.getProperty(RECORD_UPDATED));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Cancel an active ride by updating its status to CANCELLED
     *
     * @param rideDetailId The ride detail ID
     * @return ResponseEntity with success response
     */
    @PutMapping("/{rideDetailId}/cancel")
    public ResponseEntity<?> cancelRide(@PathVariable Long rideDetailId) {
        log.info("Received request to cancel ride ID: {}", rideDetailId);

        RideDetail rideDetail = rideDetailService.cancelRide(rideDetailId);

        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(rideDetail.getId());
        response.setMessages(environment.getProperty(RECORD_UPDATED));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get active ride for a driver profile (returns the latest one)
     *
     * @param driverProfileId The driver profile ID
     * @return ResponseEntity with active ride detail
     */
    @GetMapping("/driver/{driverProfileId}/active")
    public ResponseEntity<?> getActiveRideByDriver(@PathVariable Long driverProfileId) {
        log.info("Received request to get active ride for driver profile ID: {}", driverProfileId);

        RideDetailResponseResource rideDetail = rideDetailService.getActiveRideByDriverProfileId(driverProfileId);

        return new ResponseEntity<>(rideDetail, HttpStatus.OK);
    }

    /**
     * Get all rides for a driver profile, optionally filtered by status
     *
     * @param driverProfileId The driver profile ID
     * @param status Optional status filter (e.g. ACTIVE, COMPLETED)
     * @return ResponseEntity with list of ride details
     */
    @GetMapping("/driver/{driverProfileId}")
    public ResponseEntity<?> getRidesByDriver(
            @PathVariable Long driverProfileId,
            @RequestParam(value = "status", required = false) String status) {
        log.info("Received request to get rides for driver profile ID: {}, status: {}", driverProfileId, status);

        return new ResponseEntity<>(rideDetailService.getRidesByDriverProfileId(driverProfileId, status), HttpStatus.OK);
    }
}