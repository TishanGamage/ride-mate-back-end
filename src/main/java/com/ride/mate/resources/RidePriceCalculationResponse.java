package com.ride.mate.resources;

import lombok.*;

import java.math.BigDecimal;

/**
 * Ride Price Calculation Response
 * Response resource for ride price calculation
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 19-03-2026    N/A          N/A          Iruni           Initial Development
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RidePriceCalculationResponse {

    private Long driverProfileId;

    private Long vehicleTypeId;

    private String vehicleTypeName;

    private BigDecimal totalDistance;

    private BigDecimal perKmRate;

    private BigDecimal totalRidePrice;
}

