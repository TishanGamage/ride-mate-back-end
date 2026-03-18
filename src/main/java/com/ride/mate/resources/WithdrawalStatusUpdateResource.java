package com.ride.mate.resources;

import com.ride.mate.enums.WithdrawalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * WithdrawalStatusUpdateResource
 * Request payload for updating the status of a withdrawal request (admin action)
 *
 * @author Danushka
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-03-2026    N/A          N/A          Danushka          Initial Development
 */
@Getter
@Setter
public class WithdrawalStatusUpdateResource {

    @NotNull(message = "{invalid.value}")
    private WithdrawalStatus status;

    private String remarks;
}

