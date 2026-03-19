package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.WithdrawalRequest;
import com.ride.mate.resources.SuccessAndErrorDetailsResource;
import com.ride.mate.resources.WithdrawalRequestAddResource;
import com.ride.mate.resources.WithdrawalStatusUpdateResource;
import com.ride.mate.service.WithdrawalService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * WithdrawalController
 * REST API endpoints for driver withdrawal request management
 *
 * @author Danushka
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-03-2026    N/A          N/A          Danushka          Initial Development
 */
@Slf4j
@RestController
@RequestMapping(value = "/withdrawal")
@CrossOrigin(origins = "*")
public class WithdrawalController extends MessagePropertyBase {

    private final WithdrawalService withdrawalService;
    private final Environment environment;

    public WithdrawalController(WithdrawalService withdrawalService, Environment environment) {
        this.withdrawalService = withdrawalService;
        this.environment = environment;
    }

    /**
     * Create a new withdrawal request for a driver
     *
     * @param request withdrawal request details (driverProfileId, amount, bank details)
     * @return ResponseEntity with created withdrawal ID and success message
     */
    @PostMapping
    public ResponseEntity<SuccessAndErrorDetailsResource> createWithdrawalRequest(
            @Valid @RequestBody WithdrawalRequestAddResource request) {
        log.info("Received withdrawal request for driverProfileId: {}", request.getDriverProfileId());

        WithdrawalRequest withdrawal = withdrawalService.createWithdrawalRequest(request);

        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(withdrawal.getId());
        response.setMessages(environment.getProperty(RECORD_CREATED));

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Update the status of a withdrawal request (Admin action — approve or reject)
     *
     * @param id      the withdrawal request ID
     * @param request status update resource containing new status and optional remarks
     * @return ResponseEntity with updated withdrawal ID and success message
     */
    @PutMapping(value = "/{id}/status")
    public ResponseEntity<SuccessAndErrorDetailsResource> updateWithdrawalStatus(
            @PathVariable Long id,
            @Valid @RequestBody WithdrawalStatusUpdateResource request) {
        log.info("Received status update for withdrawal id: {} → {}", id, request.getStatus());

        WithdrawalRequest updated = withdrawalService.updateWithdrawalStatus(
                id, request.getStatus(), request.getRemarks());

        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(updated.getId());
        response.setMessages(environment.getProperty(RECORD_UPDATED));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get all withdrawal requests for a specific driver
     *
     * @param driverProfileId the driver profile ID
     * @return list of WithdrawalRequest entities
     */
    @GetMapping(value = "/driver/{driverProfileId}")
    public ResponseEntity<List<WithdrawalRequest>> getByDriverProfile(
            @PathVariable Long driverProfileId) {
        log.info("Fetching withdrawal requests for driverProfileId: {}", driverProfileId);
        List<WithdrawalRequest> withdrawals = withdrawalService.getByDriverProfile(driverProfileId);
        return new ResponseEntity<>(withdrawals, HttpStatus.OK);
    }

    /**
     * Get all pending withdrawal requests (Admin view)
     *
     * @return list of pending WithdrawalRequest entities
     */
    @GetMapping(value = "/pending")
    public ResponseEntity<List<WithdrawalRequest>> getAllPending() {
        log.info("Fetching all pending withdrawal requests");
        List<WithdrawalRequest> withdrawals = withdrawalService.getAllPending();
        return new ResponseEntity<>(withdrawals, HttpStatus.OK);
    }
}

