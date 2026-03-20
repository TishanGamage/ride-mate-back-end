package com.ride.mate.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DriverWalletResponseResource
 * Response DTO for driver wallet summary information including
 * available balance, total earnings, total commission, and total withdrawn
 *
 * @author Danushka
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Danushka          Initial Development
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DriverWalletResponseResource {

    private Long walletId;
    private Long driverProfileId;
    private String driverName;
    private BigDecimal availableBalance;
    private BigDecimal totalEarnings;
    private BigDecimal totalCommission;
    private BigDecimal totalWithdrawn;
    private BigDecimal totalNetEarnings;
    private String currency;
}

