package com.ride.mate.resources;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DriverTotalCostResponse
 * Response resource for driver total ride cost calculation
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 19-03-2026    N/A          N/A          Iruni          Initial Development
 */
@Getter
@Setter
public class DriverTotalCostResponse {

    private Long driverProfileId;
    private BigDecimal totalDistance;
    private BigDecimal perKmRate;
    private BigDecimal totalRideCost;
    private String vehicleTypeName;
    private String vehicleTypeCode;
}

