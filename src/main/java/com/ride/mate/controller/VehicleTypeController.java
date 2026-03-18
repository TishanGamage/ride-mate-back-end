package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.VehicleType;
import com.ride.mate.resources.SuccessAndErrorDetailsResource;
import com.ride.mate.service.VehicleTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * VehicleTypeController
 * REST API endpoints for vehicle type management
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 25-02-2026    N/A          N/A          Iruni           Initial Development
 * 2 17-03-2026    N/A          N/A          Tishan          Updated to follow coding standards
 */
@Slf4j
@RestController
@RequestMapping(value = "/vehicle-type")
@CrossOrigin(origins = "*")
public class VehicleTypeController extends MessagePropertyBase {

    private final VehicleTypeService vehicleTypeService;
    private final Environment environment;

    public VehicleTypeController(VehicleTypeService vehicleTypeService, Environment environment) {
        this.vehicleTypeService = vehicleTypeService;
        this.environment = environment;
    }

    /**
     * Get VehicleType by id
     *
     * @param id - Vehicle Type Id
     * @return VehicleType matching the id
     */
    @GetMapping(value = "/get-vehicle-type/id/{id}")
    public ResponseEntity<Object> getVehicleTypeById(@PathVariable(value = "id") Long id) {
        log.info("Received request to get vehicle type by ID: {}", id);
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();

        Optional<VehicleType> vehicleType = vehicleTypeService.findById(id);
        if (vehicleType.isPresent()) {
            return new ResponseEntity<>(vehicleType.get(), HttpStatus.OK);
        } else {
            response.setMessages(environment.getProperty(RECORD_NOT_FOUND));
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }
    }

    /**
     * Get VehicleTypes by status
     *
     * @param status - status
     * @return List of VehicleTypes matching the status
     */
    @GetMapping(value = "/get-vehicle-type/status/{status}")
    public ResponseEntity<Object> getVehicleTypeByStatus(@PathVariable(value = "status") String status) {
        log.info("Received request to get vehicle types by status: {}", status);
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();

        List<VehicleType> vehicleTypes = vehicleTypeService.findByStatus(status);
        if (vehicleTypes != null && !vehicleTypes.isEmpty()) {
            return new ResponseEntity<>(vehicleTypes, HttpStatus.OK);
        } else {
            response.setMessages(environment.getProperty(RECORD_NOT_FOUND));
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }
    }
}
