package com.ride.mate.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * WithdrawalRequestAddResource
 * Request payload for a driver to submit a withdrawal request
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
public class WithdrawalRequestAddResource {

    @NotNull(message = "{invalid.value}")
    private Long driverProfileId;

    @NotNull(message = "{invalid.value}")
    private BigDecimal amount;

    @NotBlank(message = "{can.not.be.blank}")
    private String bankName;

    @NotBlank(message = "{can.not.be.blank}")
    private String accountNumber;

    @NotBlank(message = "{can.not.be.blank}")
    private String accountHolderName;

    private String currency = "LKR";
}

