package com.ride.mate.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * PaymentTransactionResponseResource
 * Response DTO for a payment transaction record
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
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentTransactionResponseResource {

    private Long id;
    private String orderId;
    private BigDecimal payhereAmount;
    private String currency;
    private String status;
    private String method;
    private Timestamp createdDate;
}

