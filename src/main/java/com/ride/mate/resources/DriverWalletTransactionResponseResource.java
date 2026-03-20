package com.ride.mate.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * DriverWalletTransactionResponseResource
 * Response DTO for individual wallet transaction details
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
public class DriverWalletTransactionResponseResource {

    private Long transactionId;
    private String transactionType;
    private BigDecimal grossAmount;
    private BigDecimal commissionPercentage;
    private BigDecimal commissionAmount;
    private BigDecimal netAmount;
    private BigDecimal balanceAfter;
    private String currency;
    private String description;
    private Long rideDetailId;
    private String startCity;
    private Long withdrawalRequestId;
    private Timestamp createdDate;
}

