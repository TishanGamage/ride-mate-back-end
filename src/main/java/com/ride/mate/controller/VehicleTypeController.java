package com.ride.mate.controller;

import com.ride.mate.core.LoginAuthentication;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.IdentificationType;
import com.ride.mate.domain.VehicleType;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.resources.SuccessAndErrorDetailsResource;
import com.ride.mate.service.VehicleTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * VehicleType Controller
 * REST API endpoints for vehicle types

 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 25-02-2026    N/A          N/A          Iruni          Initial Development
 */

@Slf4j
@RestController
@RequestMapping(value = "/vehicle-type")
@CrossOrigin(origins = "*")
public class VehicleTypeController extends MessagePropertyBase {
    @Autowired
    private VehicleTypeService vehicleTypeService;

    /**
     * get VehicleType by id
     * @param @PathVariable{tenantId}
     * @param @PathVariable{id}
     * @return Optional<VehicleType>
     */
    @GetMapping(value = "/get-identification-type/id/{id}")
    public ResponseEntity<Object> getVehicleTypeById(
            @PathVariable(value = "id", required = true) Long id) {
        String userName = LoginAuthentication.getUserName();
        if(userName == null || userName.isEmpty()) {
            throw new ValidateRecordException("User not found","message");
        }
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        Optional<VehicleType> vehicleType = vehicleTypeService.findById(id);
        if (vehicleType.isPresent()) {
            return new ResponseEntity<>(vehicleType.get(), HttpStatus.OK);
        }
        else {
            response.setMessages(RECORD_NOT_FOUND);
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }
    }

    /**
     *get VehicleType by   status
     * @param @PathVariable{status}
     * @return List<VehicleType>
     */
    @GetMapping(value = "/get-vehicle-type/status/{status}")
    public ResponseEntity<Object> getVehicleTypeByStatus(
            @PathVariable(value = "status", required = true) String status) {
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();

        List<VehicleType> vehicleTypes = vehicleTypeService.findByStatus(status);

        if(vehicleTypes !=null && !vehicleTypes.isEmpty()) {
            return new ResponseEntity<>(vehicleTypes, HttpStatus.OK);
        }
        else {
            response.setMessages(RECORD_NOT_FOUND);
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }
    }
}
