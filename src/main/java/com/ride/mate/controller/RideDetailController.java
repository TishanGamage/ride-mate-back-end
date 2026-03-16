package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.RideDetail;
import com.ride.mate.resources.RideDetailRequestResource;
import com.ride.mate.resources.SuccessAndErrorDetailsResource;
import com.ride.mate.service.RideDetailService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping
    public ResponseEntity<?> createRideDetail(@Valid @RequestBody RideDetailRequestResource request) {
        log.info("Received request to create ride detail for driver profile ID: {}", request.getDriverProfileId());

        RideDetail rideDetail = rideDetailService.createRideDetail(request);

        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(rideDetail.getId());
        response.setMessages(environment.getProperty(RECORD_CREATED));

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }



}
