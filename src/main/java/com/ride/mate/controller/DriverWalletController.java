package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.resources.*;
import com.ride.mate.service.DriverWalletService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DriverWalletController
 * REST API endpoints for driver wallet management including
 * wallet summary, transaction history, ride income details, and earning credits
 *
 * @author Danushka
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Danushka          Initial Development
 */
@Slf4j
@RestController
@RequestMapping(value = "/driver-wallet")
@CrossOrigin(origins = "*")
public class DriverWalletController extends MessagePropertyBase {

    private final DriverWalletService driverWalletService;
    private final Environment environment;

    public DriverWalletController(DriverWalletService driverWalletService, Environment environment) {
        this.driverWalletService = driverWalletService;
        this.environment = environment;
    }

    /**
     * Get wallet summary for a driver including available balance,
     * total earnings, total commission, and total withdrawn
     *
     * @param driverProfileId the driver profile ID
     * @return ResponseEntity with wallet summary
     */
    @GetMapping("/{driverProfileId}")
    public ResponseEntity<DriverWalletResponseResource> getWalletSummary(
            @PathVariable Long driverProfileId) {
        log.info("Received request to get wallet summary for driverProfileId: {}", driverProfileId);

        DriverWalletResponseResource response = driverWalletService.getWalletByDriverProfileId(driverProfileId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get all wallet transactions for a driver including earnings, commissions, and withdrawals
     *
     * @param driverProfileId the driver profile ID
     * @return ResponseEntity with list of wallet transactions
     */
    @GetMapping("/{driverProfileId}/transactions")
    public ResponseEntity<List<DriverWalletTransactionResponseResource>> getWalletTransactions(
            @PathVariable Long driverProfileId) {
        log.info("Received request to get wallet transactions for driverProfileId: {}", driverProfileId);

        List<DriverWalletTransactionResponseResource> response =
                driverWalletService.getWalletTransactions(driverProfileId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get ride income details with commission breakdown for a driver.
     * Shows per-ride earnings including gross amount, commission percentage,
     * commission amount, and net earning.
     *
     * @param driverProfileId the driver profile ID
     * @return ResponseEntity with list of ride income details
     */
    @GetMapping("/{driverProfileId}/ride-income")
    public ResponseEntity<List<RideIncomeDetailResponseResource>> getRideIncomeDetails(
            @PathVariable Long driverProfileId) {
        log.info("Received request to get ride income details for driverProfileId: {}", driverProfileId);

        List<RideIncomeDetailResponseResource> response =
                driverWalletService.getRideIncomeDetails(driverProfileId);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Credit ride earning to driver wallet with commission calculation.
     * Commission percentage is loaded from application.properties.
     * Called after a passenger payment succeeds.
     *
     * @param request wallet credit details including gross amount
     * @return ResponseEntity with created transaction details
     */
    @PostMapping("/credit")
    public ResponseEntity<SuccessAndErrorDetailsResource> creditRideEarning(
            @Valid @RequestBody WalletCreditAddResource request) {
        log.info("Received request to credit ride earning for driverProfileId: {}, rideDetailId: {}",
                request.getDriverProfileId(), request.getRideDetailId());

        DriverWalletTransactionResponseResource transaction = driverWalletService.creditRideEarning(request);

        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(transaction.getTransactionId());
        response.setMessages(environment.getProperty(WALLET_CREDIT_SUCCESS));

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}

