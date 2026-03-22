package com.ride.mate.resources;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Ride Request Resource (DTO)
 * Request payload for a passenger requesting to join a ride
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Tishan           Initial Development
 * 2 22-03-2026    N/A          N/A          Tishan           Added optional estimatedCost field
 */
@Getter
@Setter
public class RideRequestResource {

    @NotNull(message = "{invalid.value}")
    private Long rideDetailId;

    @NotNull(message = "{invalid.value}")
    private Long userId;

    @NotNull(message = "{invalid.value}")
    private BigDecimal passengerStartLat;

    @NotNull(message = "{invalid.value}")
    private BigDecimal passengerStartLng;

    @NotNull(message = "{invalid.value}")
    private BigDecimal passengerEndLat;

    @NotNull(message = "{invalid.value}")
    private BigDecimal passengerEndLng;

    @NotNull(message = "{invalid.value}")
    private BigDecimal passengerRideDistance;

    /** Optional: pre-calculated estimated cost from the estimate-cost API */
    private BigDecimal estimatedCost;

    private String startCity;

    private String endCity;
}

