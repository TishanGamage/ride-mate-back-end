package com.ride.mate.resources;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Admin Withdrawal Status Request
 * Request payload for updating withdrawal request status by admin
 *
 * @author RideMate
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 02-04-2026    N/A          N/A          RideMate         Initial Development
 */
@Getter
@Setter
public class AdminWithdrawalStatusRequest {

    @NotBlank(message = "{can.not.be.blank}")
    private String status; // APPROVED | REJECTED

    private String remarks;
}

