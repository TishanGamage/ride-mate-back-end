package com.ride.mate.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * RideIncomeDetailResponseResource
 * Response DTO for per-ride income breakdown showing gross earnings,
 * commission details, and net earnings for a specific ride
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
public class RideIncomeDetailResponseResource {

    private Long rideDetailId;
    private String startCity;
    private BigDecimal totalRideDistance;
    private BigDecimal totalRideCost;
    private BigDecimal grossEarning;
    private BigDecimal commissionPercentage;
    private BigDecimal commissionAmount;
    private BigDecimal netEarning;
    private String currency;
    private Long numberOfPassengers;
    private Timestamp rideDate;
    private Timestamp earnedDate;
}

