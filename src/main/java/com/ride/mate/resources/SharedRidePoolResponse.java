package com.ride.mate.resources;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Shared Ride Pool Response (DTO)
 * Response payload with available shared ride pool information
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Iruni           Initial Development
 */
@Getter
@Setter
public class SharedRidePoolResponse {

    private Long rideDetailId;

    private Long driverProfileId;

    private String startCity;

    private String endCity;

    private BigDecimal startLocationLatitude;

    private BigDecimal startLocationLongitude;

    private BigDecimal endLocationLatitude;

    private BigDecimal endLocationLongitude;

    private Timestamp startTime;

    private Long currentPassengers;

    private Long availableSeats;

    private BigDecimal totalRideDistance;

    private BigDecimal totalRideCost;

    private BigDecimal perKmRate;

    private BigDecimal estimatedCostPerPassenger;

    private BigDecimal driverRating;

    private Long totalRidesAsDriver;

    private Double mlAcceptanceProbability;

    private Integer mlRank;
}
