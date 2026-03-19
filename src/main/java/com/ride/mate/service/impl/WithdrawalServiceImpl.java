package com.ride.mate.service.impl;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.DriverEarning;
import com.ride.mate.domain.DriverProfile;
import com.ride.mate.domain.WithdrawalRequest;
import com.ride.mate.enums.PaymentStatus;
import com.ride.mate.enums.WithdrawalStatus;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.DriverEarningRepository;
import com.ride.mate.repository.DriverProfileRepository;
import com.ride.mate.repository.WithdrawalRequestRepository;
import com.ride.mate.resources.WithdrawalRequestAddResource;
import com.ride.mate.service.WithdrawalService;
import com.ride.mate.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * WithdrawalServiceImpl
 * Service implementation for driver withdrawal request management.
 * Validates available balance and manages the full withdrawal lifecycle.
 *
 * @author Danushka
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-03-2026    N/A          N/A          Danushka          Initial Development
 */@Slf4j
@Service
@Transactional
public class WithdrawalServiceImpl extends MessagePropertyBase implements WithdrawalService {

    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final DriverEarningRepository driverEarningRepository;
    private final Environment environment;

    public WithdrawalServiceImpl(WithdrawalRequestRepository withdrawalRequestRepository,
                                 DriverProfileRepository driverProfileRepository,
                                 DriverEarningRepository driverEarningRepository,
                                 Environment environment) {
        this.withdrawalRequestRepository = withdrawalRequestRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.driverEarningRepository = driverEarningRepository;
        this.environment = environment;
    }

    @Override
    public WithdrawalRequest createWithdrawalRequest(WithdrawalRequestAddResource request) {
        log.info("Processing withdrawal request for driverProfileId: {}", request.getDriverProfileId());

        // 1. Validate driver profile exists
        DriverProfile driverProfile = driverProfileRepository.findById(request.getDriverProfileId())
                .orElseThrow(() -> {
                    log.warn("Driver profile not found for id: {}", request.getDriverProfileId());
                    return new ValidateRecordException(environment.getProperty(DRIVER_PROFILE_NOT_FOUND), "message");
                });

        // 2. Check available balance (sum of PENDING earnings not yet paid out)
        BigDecimal availableBalance = driverEarningRepository.sumAmountByDriverProfileIdAndStatus(
                request.getDriverProfileId(), PaymentStatus.PENDING);

        if (availableBalance == null) {
            availableBalance = BigDecimal.ZERO;
        }

        if (request.getAmount().compareTo(availableBalance) > 0) {
            log.warn("Insufficient balance for driverProfileId: {}. Requested: {}, Available: {}",
                    request.getDriverProfileId(), request.getAmount(), availableBalance);
            throw new ValidateRecordException(environment.getProperty(WITHDRAWAL_INSUFFICIENT_BALANCE), "message");
        }

        // 3. Create withdrawal request
        WithdrawalRequest withdrawalRequest = new WithdrawalRequest();
        withdrawalRequest.setDriverProfile(driverProfile);
        withdrawalRequest.setAmount(request.getAmount());
        withdrawalRequest.setCurrency(request.getCurrency() != null ? request.getCurrency() : "LKR");
        withdrawalRequest.setBankName(request.getBankName());
        withdrawalRequest.setAccountNumber(request.getAccountNumber());
        withdrawalRequest.setAccountHolderName(request.getAccountHolderName());
        withdrawalRequest.setStatus(WithdrawalStatus.PENDING);
        withdrawalRequest.setCreatedDate(DateUtil.getDate());
        withdrawalRequest.setCreatedUser(SYSTEM);

        WithdrawalRequest saved = withdrawalRequestRepository.save(withdrawalRequest);
        log.info("Withdrawal request created with ID: {} for driverProfileId: {}", saved.getId(), request.getDriverProfileId());
        return saved;
    }

    @Override
    public WithdrawalRequest updateWithdrawalStatus(Long id, WithdrawalStatus status, String remarks) {
        log.info("Updating withdrawal request id: {} to status: {}", id, status);

        WithdrawalRequest withdrawalRequest = withdrawalRequestRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Withdrawal request not found for id: {}", id);
                    return new ValidateRecordException(environment.getProperty(WITHDRAWAL_REQUEST_NOT_FOUND), "message");
                });

        withdrawalRequest.setStatus(status);
        withdrawalRequest.setRemarks(remarks);
        withdrawalRequest.setModifiedDate(DateUtil.getDate());
        withdrawalRequest.setModifiedUser(SYSTEM);

        // If approved, mark matching earnings as PAID
        if (WithdrawalStatus.APPROVED.equals(status)) {
            List<DriverEarning> pendingEarnings = driverEarningRepository
                    .findByDriverProfileIdAndStatus(withdrawalRequest.getDriverProfile().getId(), PaymentStatus.PENDING);

            BigDecimal remaining = withdrawalRequest.getAmount();
            for (DriverEarning earning : pendingEarnings) {
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
                earning.setStatus(PaymentStatus.SUCCESS);
                earning.setModifiedDate(DateUtil.getDate());
                earning.setModifiedUser(SYSTEM);
                driverEarningRepository.save(earning);
                remaining = remaining.subtract(earning.getAmount());
            }
            log.info("Marked earnings as PAID for driverProfileId: {}", withdrawalRequest.getDriverProfile().getId());
        }

        WithdrawalRequest updated = withdrawalRequestRepository.save(withdrawalRequest);
        log.info("Withdrawal request {} updated to status: {}", id, status);
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WithdrawalRequest> getByDriverProfile(Long driverProfileId) {
        log.info("Fetching withdrawal requests for driverProfileId: {}", driverProfileId);
        return withdrawalRequestRepository.findByDriverProfileId(driverProfileId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WithdrawalRequest> getAllPending() {
        log.info("Fetching all pending withdrawal requests");
        return withdrawalRequestRepository.findByStatus(WithdrawalStatus.PENDING);
    }
}

