package com.ride.mate.resources;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Share Ride Detail Add Resource (DTO)
 * Request payload for joining a shared ride
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
public class ShareRideDetailAddResource {

    @NotNull(message = "{invalid.value}")
    private Long rideDetailId;

    @NotNull(message = "{invalid.value}")
    private Long userId;

    private Long rideRequestId;

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

    private String startCity;

    private String endCity;
}
