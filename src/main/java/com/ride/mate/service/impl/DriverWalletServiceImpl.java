package com.ride.mate.service.impl;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.*;
import com.ride.mate.enums.WalletTransactionType;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.*;
import com.ride.mate.resources.*;
import com.ride.mate.service.DriverWalletService;
import com.ride.mate.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * DriverWalletServiceImpl
 * Service implementation for driver wallet operations.
 * Manages wallet initialization, ride earning credits with commission calculation,
 * withdrawal debits, and wallet/transaction/ride income retrieval.
 * Commission percentage is loaded from application.properties (ride-mate.commission.default-percentage).
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Tishan          Initial Development
 * 2 20-03-2026    N/A          N/A          Tishan          Commission percentage from application.properties
 */
@Slf4j
@Service
@Transactional
public class DriverWalletServiceImpl extends MessagePropertyBase implements DriverWalletService {

    private final DriverWalletRepository driverWalletRepository;
    private final DriverWalletTransactionRepository driverWalletTransactionRepository;
    private final RideDetailRepository rideDetailRepository;
    private final DriverEarningRepository driverEarningRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final Environment environment;
    private final BigDecimal defaultCommissionPercentage;

    public DriverWalletServiceImpl(DriverWalletRepository driverWalletRepository,
                                   DriverWalletTransactionRepository driverWalletTransactionRepository,
                                   RideDetailRepository rideDetailRepository,
                                   DriverEarningRepository driverEarningRepository,
                                   WithdrawalRequestRepository withdrawalRequestRepository,
                                   Environment environment,
                                   @Value("${ride-mate.commission.default-percentage}") BigDecimal defaultCommissionPercentage) {
        this.driverWalletRepository = driverWalletRepository;
        this.driverWalletTransactionRepository = driverWalletTransactionRepository;
        this.rideDetailRepository = rideDetailRepository;
        this.driverEarningRepository = driverEarningRepository;
        this.withdrawalRequestRepository = withdrawalRequestRepository;
        this.environment = environment;
        this.defaultCommissionPercentage = defaultCommissionPercentage;
    }

    @Override
    public DriverWallet initializeWallet(DriverProfile driverProfile) {
        log.info("Initializing wallet for driverProfileId: {}", driverProfile.getId());

        if (driverWalletRepository.existsByDriverProfileId(driverProfile.getId())) {
            log.warn("Wallet already exists for driverProfileId: {}", driverProfile.getId());
            throw new ValidateRecordException(environment.getProperty(WALLET_ALREADY_EXISTS), "message");
        }

        DriverWallet wallet = new DriverWallet();
        wallet.setDriverProfile(driverProfile);
        wallet.setAvailableBalance(BigDecimal.ZERO);
        wallet.setTotalEarnings(BigDecimal.ZERO);
        wallet.setTotalCommission(BigDecimal.ZERO);
        wallet.setTotalWithdrawn(BigDecimal.ZERO);
        wallet.setCurrency("LKR");
        wallet.setCreatedDate(DateUtil.getDate());
        wallet.setCreatedUser(SYSTEM);

        DriverWallet saved = driverWalletRepository.save(wallet);
        log.info("Wallet initialized successfully with ID: {} for driverProfileId: {}", saved.getId(), driverProfile.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public DriverWalletResponseResource getWalletByDriverProfileId(Long driverProfileId) {
        log.info("Fetching wallet for driverProfileId: {}", driverProfileId);

        DriverWallet wallet = driverWalletRepository.findByDriverProfileId(driverProfileId)
                .orElseThrow(() -> {
                    log.warn("Wallet not found for driverProfileId: {}", driverProfileId);
                    return new ValidateRecordException(environment.getProperty(WALLET_NOT_FOUND), "message");
                });

        return mapToWalletResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverWalletTransactionResponseResource> getWalletTransactions(Long driverProfileId) {
        log.info("Fetching wallet transactions for driverProfileId: {}", driverProfileId);

        // Validate wallet exists
        driverWalletRepository.findByDriverProfileId(driverProfileId)
                .orElseThrow(() -> {
                    log.warn("Wallet not found for driverProfileId: {}", driverProfileId);
                    return new ValidateRecordException(environment.getProperty(WALLET_NOT_FOUND), "message");
                });

        List<DriverWalletTransaction> transactions =
                driverWalletTransactionRepository.findByDriverWalletDriverProfileIdOrderByCreatedDateDesc(driverProfileId);

        List<DriverWalletTransactionResponseResource> responseList = new ArrayList<>();
        for (DriverWalletTransaction transaction : transactions) {
            responseList.add(mapToTransactionResponse(transaction));
        }

        log.info("Found {} wallet transactions for driverProfileId: {}", responseList.size(), driverProfileId);
        return responseList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RideIncomeDetailResponseResource> getRideIncomeDetails(Long driverProfileId) {
        log.info("Fetching ride income details for driverProfileId: {}", driverProfileId);

        // Validate wallet exists
        driverWalletRepository.findByDriverProfileId(driverProfileId)
                .orElseThrow(() -> {
                    log.warn("Wallet not found for driverProfileId: {}", driverProfileId);
                    return new ValidateRecordException(environment.getProperty(WALLET_NOT_FOUND), "message");
                });

        // Get only RIDE_EARNING transactions
        List<DriverWalletTransaction> rideEarnings =
                driverWalletTransactionRepository.findByDriverWalletDriverProfileIdAndTransactionTypeOrderByCreatedDateDesc(
                        driverProfileId, WalletTransactionType.RIDE_EARNING);

        List<RideIncomeDetailResponseResource> responseList = new ArrayList<>();
        for (DriverWalletTransaction transaction : rideEarnings) {
            responseList.add(mapToRideIncomeResponse(transaction));
        }

        log.info("Found {} ride income records for driverProfileId: {}", responseList.size(), driverProfileId);
        return responseList;
    }

    @Override
    public DriverWalletTransactionResponseResource creditRideEarning(WalletCreditAddResource request) {
        log.info("Processing ride earning credit for driverProfileId: {}, rideDetailId: {}",
                request.getDriverProfileId(), request.getRideDetailId());


        // Lock wallet for update to prevent concurrent modifications
        DriverWallet wallet = driverWalletRepository.findByDriverProfileIdForUpdate(request.getDriverProfileId())
                .orElseThrow(() -> {
                    log.warn("Wallet not found for driverProfileId: {}", request.getDriverProfileId());
                    return new ValidateRecordException(environment.getProperty(WALLET_NOT_FOUND), "message");
                });

        // Validate ride detail exists
        RideDetail rideDetail = rideDetailRepository.findById(request.getRideDetailId())
                .orElseThrow(() -> {
                    log.warn("Ride detail not found for id: {}", request.getRideDetailId());
                    return new ValidateRecordException(environment.getProperty(RIDE_DETAIL_NOT_FOUND), "message");
                });

        // Lookup driver earning if provided
        DriverEarning driverEarning = null;
        if (request.getDriverEarningId() != null) {
            driverEarning = driverEarningRepository.findById(request.getDriverEarningId())
                    .orElse(null);
        }

        // Calculate commission and net amount using default commission from application.properties
        BigDecimal grossAmount = request.getGrossAmount();
        BigDecimal commissionPercentage = defaultCommissionPercentage;
        BigDecimal commissionAmount = grossAmount.multiply(commissionPercentage)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal netAmount = grossAmount.subtract(commissionAmount);

        // Update wallet balances
        wallet.setTotalEarnings(wallet.getTotalEarnings().add(grossAmount));
        wallet.setTotalCommission(wallet.getTotalCommission().add(commissionAmount));
        wallet.setAvailableBalance(wallet.getAvailableBalance().add(netAmount));
        wallet.setModifiedDate(DateUtil.getDate());
        wallet.setModifiedUser(SYSTEM);
        driverWalletRepository.save(wallet);

        // Create wallet transaction
        DriverWalletTransaction transaction = new DriverWalletTransaction();
        transaction.setDriverWallet(wallet);
        transaction.setRideDetail(rideDetail);
        transaction.setDriverEarning(driverEarning);
        transaction.setTransactionType(WalletTransactionType.RIDE_EARNING);
        transaction.setGrossAmount(grossAmount);
        transaction.setCommissionPercentage(commissionPercentage);
        transaction.setCommissionAmount(commissionAmount);
        transaction.setNetAmount(netAmount);
        transaction.setBalanceAfter(wallet.getAvailableBalance());
        transaction.setCurrency(request.getCurrency() != null ? request.getCurrency() : "LKR");
        transaction.setDescription(request.getDescription() != null ? request.getDescription()
                : "Ride earning from ride #" + rideDetail.getId()
                + " | Gross: " + grossAmount
                + " | Commission (" + commissionPercentage + "%): " + commissionAmount
                + " | Net: " + netAmount);
        transaction.setCreatedDate(DateUtil.getDate());
        transaction.setCreatedUser(SYSTEM);

        DriverWalletTransaction saved = driverWalletTransactionRepository.save(transaction);
        log.info("Ride earning credited to wallet. TransactionId: {}, GrossAmount: {}, Commission: {}, NetAmount: {}, NewBalance: {}",
                saved.getId(), grossAmount, commissionAmount, netAmount, wallet.getAvailableBalance());

        return mapToTransactionResponse(saved);
    }

    @Override
    public DriverWalletTransactionResponseResource debitWithdrawal(Long driverProfileId, BigDecimal amount, Long withdrawalRequestId) {
        log.info("Processing withdrawal debit for driverProfileId: {}, amount: {}", driverProfileId, amount);

        // Lock wallet for update
        DriverWallet wallet = driverWalletRepository.findByDriverProfileIdForUpdate(driverProfileId)
                .orElseThrow(() -> {
                    log.warn("Wallet not found for driverProfileId: {}", driverProfileId);
                    return new ValidateRecordException(environment.getProperty(WALLET_NOT_FOUND), "message");
                });

        // Validate sufficient balance
        if (amount.compareTo(wallet.getAvailableBalance()) > 0) {
            log.warn("Insufficient wallet balance for driverProfileId: {}. Requested: {}, Available: {}",
                    driverProfileId, amount, wallet.getAvailableBalance());
            throw new ValidateRecordException(environment.getProperty(WALLET_INSUFFICIENT_BALANCE), "message");
        }

        // Lookup withdrawal request if provided
        WithdrawalRequest withdrawalRequest = null;
        if (withdrawalRequestId != null) {
            withdrawalRequest = withdrawalRequestRepository.findById(withdrawalRequestId)
                    .orElse(null);
        }

        // Update wallet balances
        wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(amount));
        wallet.setTotalWithdrawn(wallet.getTotalWithdrawn().add(amount));
        wallet.setModifiedDate(DateUtil.getDate());
        wallet.setModifiedUser(SYSTEM);
        driverWalletRepository.save(wallet);

        // Create withdrawal transaction
        DriverWalletTransaction transaction = new DriverWalletTransaction();
        transaction.setDriverWallet(wallet);
        transaction.setWithdrawalRequest(withdrawalRequest);
        transaction.setTransactionType(WalletTransactionType.WITHDRAWAL);
        transaction.setGrossAmount(amount);
        transaction.setCommissionPercentage(BigDecimal.ZERO);
        transaction.setCommissionAmount(BigDecimal.ZERO);
        transaction.setNetAmount(amount.negate());
        transaction.setBalanceAfter(wallet.getAvailableBalance());
        transaction.setCurrency(wallet.getCurrency());
        transaction.setDescription("Withdrawal of " + amount + " " + wallet.getCurrency());
        transaction.setCreatedDate(DateUtil.getDate());
        transaction.setCreatedUser(SYSTEM);

        DriverWalletTransaction saved = driverWalletTransactionRepository.save(transaction);
        log.info("Withdrawal debited from wallet. TransactionId: {}, Amount: {}, NewBalance: {}",
                saved.getId(), amount, wallet.getAvailableBalance());

        return mapToTransactionResponse(saved);
    }

    // ======================== MAPPING HELPERS ========================

    private DriverWalletResponseResource mapToWalletResponse(DriverWallet wallet) {
        DriverWalletResponseResource response = new DriverWalletResponseResource();
        response.setWalletId(wallet.getId());
        response.setDriverProfileId(wallet.getDriverProfile().getId());

        // Build driver name from the user associated with the driver profile
        User user = wallet.getDriverProfile().getUser();
        if (user != null) {
            String driverName = (user.getFirstName() != null ? user.getFirstName() : "")
                    + " " + (user.getLastName() != null ? user.getLastName() : "");
            response.setDriverName(driverName.trim());
        }

        response.setAvailableBalance(wallet.getAvailableBalance());
        response.setTotalEarnings(wallet.getTotalEarnings());
        response.setTotalCommission(wallet.getTotalCommission());
        response.setTotalWithdrawn(wallet.getTotalWithdrawn());
        response.setTotalNetEarnings(wallet.getTotalEarnings().subtract(wallet.getTotalCommission()));
        response.setCurrency(wallet.getCurrency());
        return response;
    }

    private DriverWalletTransactionResponseResource mapToTransactionResponse(DriverWalletTransaction transaction) {
        DriverWalletTransactionResponseResource response = new DriverWalletTransactionResponseResource();
        response.setTransactionId(transaction.getId());
        response.setTransactionType(transaction.getTransactionType().name());
        response.setGrossAmount(transaction.getGrossAmount());
        response.setCommissionPercentage(transaction.getCommissionPercentage());
        response.setCommissionAmount(transaction.getCommissionAmount());
        response.setNetAmount(transaction.getNetAmount());
        response.setBalanceAfter(transaction.getBalanceAfter());
        response.setCurrency(transaction.getCurrency());
        response.setDescription(transaction.getDescription());
        response.setCreatedDate(transaction.getCreatedDate());

        if (transaction.getRideDetail() != null) {
            response.setRideDetailId(transaction.getRideDetail().getId());
            response.setStartCity(transaction.getRideDetail().getStartCity());
        }

        if (transaction.getWithdrawalRequest() != null) {
            response.setWithdrawalRequestId(transaction.getWithdrawalRequest().getId());
        }

        return response;
    }

    private RideIncomeDetailResponseResource mapToRideIncomeResponse(DriverWalletTransaction transaction) {
        RideIncomeDetailResponseResource response = new RideIncomeDetailResponseResource();

        if (transaction.getRideDetail() != null) {
            RideDetail ride = transaction.getRideDetail();
            response.setRideDetailId(ride.getId());
            response.setStartCity(ride.getStartCity());
            response.setTotalRideDistance(ride.getTotalRideDistance());
            response.setTotalRideCost(ride.getTotalRideCost());
            response.setRideDate(ride.getStartTime());
        }

        response.setGrossEarning(transaction.getGrossAmount());
        response.setCommissionPercentage(transaction.getCommissionPercentage());
        response.setCommissionAmount(transaction.getCommissionAmount());
        response.setNetEarning(transaction.getNetAmount());
        response.setCurrency(transaction.getCurrency());
        response.setEarnedDate(transaction.getCreatedDate());

        return response;
    }
}

