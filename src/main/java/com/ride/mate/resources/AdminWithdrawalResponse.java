package com.ride.mate.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Admin Withdrawal Response
 * Response payload for withdrawal request details in admin panel
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
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminWithdrawalResponse {

    private Long id;
    private Long driverProfileId;
    private String driverName;
    private String driverEmail;
    private String amount;
    private String currency;
    private String bankName;
    private String accountNumber;
    private String accountHolderName;
    private String status;
    private String remarks;
    private String createdDate;
}

