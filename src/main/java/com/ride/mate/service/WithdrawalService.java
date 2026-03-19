package com.ride.mate.service;

import com.ride.mate.domain.WithdrawalRequest;
import com.ride.mate.enums.WithdrawalStatus;
import com.ride.mate.resources.WithdrawalRequestAddResource;

import java.util.List;

/**
 * WithdrawalService
 * Business logic interface for driver withdrawal request operations
 *
 * @author Danushka
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-03-2026    N/A          N/A          Danushka          Initial Development
 */
public interface WithdrawalService {

    WithdrawalRequest createWithdrawalRequest(WithdrawalRequestAddResource request);

    WithdrawalRequest updateWithdrawalStatus(Long id, WithdrawalStatus status, String remarks);

    List<WithdrawalRequest> getByDriverProfile(Long driverProfileId);

    List<WithdrawalRequest> getAllPending();
}

