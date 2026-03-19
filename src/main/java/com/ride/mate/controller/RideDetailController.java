package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.RideDetail;
import com.ride.mate.resources.PassengerRideConfirmRequestResource;
import com.ride.mate.resources.PassengerRideConfirmResponse;
import com.ride.mate.resources.RideDetailRequestResource;
import com.ride.mate.resources.RidePriceCalculationResponse;
import com.ride.mate.resources.SuccessAndErrorDetailsResource;
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
 */

@Slf4j
@RestController
@RequestMapping(value = "/ride-details")
@CrossOrigin(origins = "*")
public class RideDetailController extends MessagePropertyBase {

    private final RideDetailService rideDetailService;
    private final Environment environment;

    public RideDetailController(RideDetailService rideDetailService,
                                Environment environment) {
        this.rideDetailService = rideDetailService;
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
     * Confirm a passenger joining a ride
     *
     * @param request PassengerRideConfirmRequestResource containing booking details
     * @return ResponseEntity with booking confirmation
     */
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPassengerRide(@Valid @RequestBody PassengerRideConfirmRequestResource request) {
        log.info("Received passenger ride confirmation request for ride ID: {} by user ID: {}",
                request.getRideDetailId(), request.getUserId());

        PassengerRideConfirmResponse response = rideDetailService.confirmPassengerRide(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


}