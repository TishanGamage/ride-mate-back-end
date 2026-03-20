package com.ride.mate.service;

import com.ride.mate.domain.DriverProfile;
import com.ride.mate.domain.DriverWallet;
import com.ride.mate.resources.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * DriverWalletService
 * Business logic interface for driver wallet operations including
 * wallet initialization, ride earning credits, withdrawal debits,
 * and wallet/transaction retrieval.
 * Commission percentage is configured in application.properties (ride-mate.commission.default-percentage).
 *
 * @author Danushka
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Danushka          Initial Development
 * 2 20-03-2026    N/A          N/A          Danushka          Commission percentage from application.properties
 */
public interface DriverWalletService {

    /**
     * Initialize a wallet for a new driver profile with zero balances
     *
     * @param driverProfile the driver profile to create a wallet for
     * @return the created DriverWallet
     */
    DriverWallet initializeWallet(DriverProfile driverProfile);

    /**
     * Get wallet summary by driver profile ID
     *
     * @param driverProfileId the driver profile ID
     * @return wallet summary response
     */
    DriverWalletResponseResource getWalletByDriverProfileId(Long driverProfileId);

    /**
     * Get all wallet transactions for a driver
     *
     * @param driverProfileId the driver profile ID
     * @return list of transaction details
     */
    List<DriverWalletTransactionResponseResource> getWalletTransactions(Long driverProfileId);

    /**
     * Get ride income details with commission breakdown for a driver
     *
     * @param driverProfileId the driver profile ID
     * @return list of ride income details
     */
    List<RideIncomeDetailResponseResource> getRideIncomeDetails(Long driverProfileId);

    /**
     * Credit ride earning to driver wallet with commission calculation.
     * Commission percentage is loaded from application.properties.
     *
     * @param request the wallet credit request containing gross amount
     * @return the created wallet transaction
     */
    DriverWalletTransactionResponseResource creditRideEarning(WalletCreditAddResource request);

    /**
     * Debit withdrawal amount from driver wallet
     *
     * @param driverProfileId the driver profile ID
     * @param amount the withdrawal amount
     * @param withdrawalRequestId the associated withdrawal request ID
     * @return the created wallet transaction
     */
    DriverWalletTransactionResponseResource debitWithdrawal(Long driverProfileId, BigDecimal amount, Long withdrawalRequestId);
}

