package com.ride.mate.resources;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Passenger Ride Confirm Request DTO
 * Request payload for a passenger confirming/joining a ride
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Tishan           Initial Development
 */
@Getter
@Setter
public class PassengerRideConfirmRequestResource {

    @NotNull(message = "{invalid.value}")
    private Long rideDetailId;

    @NotNull(message = "{invalid.value}")
    private Long userId;

    @NotNull(message = "{invalid.value}")
    private BigDecimal startLocationLongitude;

    @NotNull(message = "{invalid.value}")
    private BigDecimal startLocationLatitude;

    @NotNull(message = "{invalid.value}")
    private BigDecimal endLocationLongitude;

    @NotNull(message = "{invalid.value}")
    private BigDecimal endLocationLatitude;

    @NotNull(message = "{invalid.value}")
    private BigDecimal passengerRideDistance;

    private String startCity;

    private String endCity;
}

