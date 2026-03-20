package com.ride.mate.resources;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * WalletCreditAddResource
 * Request payload for crediting ride earnings to a driver's wallet.
 * Used after a passenger payment succeeds to record the driver's earning
 * with commission deduction details.
 * Commission percentage is configured in application.properties (ride-mate.commission.default-percentage).
 *
 * @author Danushka
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Danushka          Initial Development
 * 2 20-03-2026    N/A          N/A          Danushka          Removed commissionPercentage (now from application.properties)
 */
@Getter
@Setter
public class WalletCreditAddResource {

    @NotNull(message = "{invalid.value}")
    private Long driverProfileId;

    @NotNull(message = "{invalid.value}")
    private Long rideDetailId;

    private Long driverEarningId;

    @NotNull(message = "{invalid.value}")
    private BigDecimal grossAmount;


    private String currency = "LKR";

    private String description;
}

