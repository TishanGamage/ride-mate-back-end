package com.ride.mate.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * PaymentInitResource
 * Request payload for initiating a passenger payment using a saved card token
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
public class PaymentInitResource {

    @NotNull(message = "{invalid.value}")
    private Long userId;

    @NotNull(message = "{invalid.value}")
    private Long rideDetailId;

    @NotBlank(message = "{can.not.be.blank}")
    private String currency;

    @NotNull(message = "{invalid.value}")
    private BigDecimal amount;

    private String itemName;
}

