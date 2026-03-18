package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.VehicleModel;
import com.ride.mate.resources.SuccessAndErrorDetailsResource;
import com.ride.mate.service.VehicleModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * VehicleModelController
 * REST API endpoints for vehicle model management
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-03-2026    N/A          N/A          Iruni           Initial Development
 */
@Slf4j
@RestController
@RequestMapping(value = "/vehicle-model")
@CrossOrigin(origins = "*")
public class VehicleModelController extends MessagePropertyBase {

    private final VehicleModelService vehicleModelService;
    private final Environment environment;

    public VehicleModelController(VehicleModelService vehicleModelService, Environment environment) {
        this.vehicleModelService = vehicleModelService;
        this.environment = environment;
    }

    /**
     * Get VehicleModels by vehicle make id and status
     *
     * @param vehicleMakeId - Vehicle Make Id
     * @param status - Status
     * @return List of VehicleModel records
     */
    @GetMapping(value = "/get-vehicle-models/vehicle-make-id/{vehicleMakeId}/status/{status}")
    public ResponseEntity<Object> getVehicleModelsByMakeIdAndStatus(
                                           @PathVariable(value = "vehicleMakeId") Long vehicleMakeId,
                                            @PathVariable(value = "status") String status) {
        log.info("Received request to get vehicle models by vehicle make ID: {} and status: {}", vehicleMakeId, status);

        List<VehicleModel> vehicleModels = vehicleModelService.findByVehicleMakeIdAndStatus(vehicleMakeId, status);

        if (!vehicleModels.isEmpty()) {
            log.info("Found {} vehicle model(s) for vehicle make ID: {} with status: {}", vehicleModels.size(), vehicleMakeId, status);
            return new ResponseEntity<>(vehicleModels, HttpStatus.OK);
        } else {
            log.info("No vehicle models found for vehicle make ID: {} with status: {}", vehicleMakeId, status);
            SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
            response.setMessages(environment.getProperty(RECORD_NOT_FOUND));
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }
    }
}
