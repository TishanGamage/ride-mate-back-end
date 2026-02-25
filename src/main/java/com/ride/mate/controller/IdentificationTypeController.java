package com.ride.mate.controller;

import com.ride.mate.domain.IdentificationType;
import com.ride.mate.resources.SuccessAndErrorDetailsResource;
import com.ride.mate.service.IdentificationTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static com.ride.mate.core.MessagePropertyBase.*;

/**
 * Identification Controller
 * REST API endpoints for identification types

 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 23-02-2026    N/A          N/A          Iruni          Initial Development
 */

@RestController
@RequestMapping(value = "/identification-type")
@CrossOrigin(origins = "*")
public class IdentificationTypeController {


    @Autowired
    private IdentificationTypeService identificationTypeService;

    /**
     * get IdentificationType by id
     * @param @PathVariable{tenantId}
     * @param @PathVariable{id}
     * @return Optional<IdentificationType>
     */
    @GetMapping(value = "/get-identification-type/id/{id}")
    public ResponseEntity<Object> getIdentificationTypeById(
            @PathVariable(value = "id", required = true) Long id) {
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        Optional<IdentificationType> identificationType = identificationTypeService.findById(id);
        if (identificationType.isPresent()) {
            return new ResponseEntity<>(identificationType.get(), HttpStatus.OK);
        }
        else {
            response.setMessages(RECORD_NOT_FOUND);
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }
    }

    /**
     *get IdentificationType by   status
     * @param @PathVariable{status}
     * @return List<IdentificationType>
     */
    @GetMapping(value = "/get-identification-type/status/{status}")
    public ResponseEntity<Object> getIdentificationTypeByStatus(
            @PathVariable(value = "status", required = true) String status) {
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();

        List<IdentificationType> identificationTypes = identificationTypeService.findByStatus(status);

        if(identificationTypes !=null && !identificationTypes.isEmpty()) {
            return new ResponseEntity<>(identificationTypes, HttpStatus.OK);
        }
        else {
            response.setMessages(RECORD_NOT_FOUND);
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }

    }

}
