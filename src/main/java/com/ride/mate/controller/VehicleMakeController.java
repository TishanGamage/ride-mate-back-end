package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.VehicleMake;
import com.ride.mate.resources.SuccessAndErrorDetailsResource;
import com.ride.mate.service.VehicleMakeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * VehicleMakeController
 * REST API endpoints for vehicle make management
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 17-03-2026    N/A          N/A          Tishan          Initial Development
 */
@Slf4j
@RestController
@RequestMapping(value = "/vehicle-make")
@CrossOrigin(origins = "*")
public class VehicleMakeController extends MessagePropertyBase {

    private final VehicleMakeService vehicleMakeService;
    private final Environment environment;

    public VehicleMakeController(VehicleMakeService vehicleMakeService, Environment environment) {
        this.vehicleMakeService = vehicleMakeService;
        this.environment = environment;
    }

    /**
     * Get VehicleMake by id
     *
     * @param id - Vehicle Make Id
     * @return Optional of VehicleMake
     */
    @GetMapping(value = "/get-vehicle-make/id/{id}")
    public ResponseEntity<Object> getVehicleMakeById(@PathVariable(value = "id") Long id) {
        log.info("Received request to get vehicle make by ID: {}", id);
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();

        Optional<VehicleMake> vehicleMake = vehicleMakeService.findById(id);
        if (vehicleMake.isPresent()) {
            return new ResponseEntity<>(vehicleMake.get(), HttpStatus.OK);
        } else {
            response.setMessages(environment.getProperty(RECORD_NOT_FOUND));
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }
    }

    /**
     * Get VehicleMakes by status
     *
     * @param status - status
     * @return List of VehicleMakes matching the status
     */
    @GetMapping(value = "/get-vehicle-make/status/{status}")
    public ResponseEntity<Object> getVehicleMakeByStatus(@PathVariable(value = "status") String status) {
        log.info("Received request to get vehicle makes by status: {}", status);
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();

        List<VehicleMake> vehicleMakes = vehicleMakeService.findByStatus(status);
        if (vehicleMakes != null && !vehicleMakes.isEmpty()) {
            return new ResponseEntity<>(vehicleMakes, HttpStatus.OK);
        } else {
            response.setMessages(environment.getProperty(RECORD_NOT_FOUND));
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }
    }
}

