package com.ride.mate.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Ride Detail Request DTO
 * Request payload for creating/updating ride details
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 15-03-2026    N/A          N/A          Iruni           Initial Development
 */
@Getter
@Setter
public class RideDetailRequestResource {

    @NotNull(message = "{invalid.value}")
    private Long driverProfileId;

    @NotNull(message = "{invalid.value}")
    private BigDecimal startLocationLongitude;

    @NotNull(message = "{invalid.value}")
    private BigDecimal endLocationLongitude;

    @NotBlank(message = "{can.not.be.blank}")
    private String startCity;

    @NotNull(message = "{invalid.value}")
    private Long availableSeats;

    @NotBlank(message = "{can.not.be.blank}")
    private String startTime;

    @NotNull(message = "{invalid.value}")
    private BigDecimal totalRideDistance;

    private String tripRoute;

    @NotBlank(message = "{can.not.be.blank}")
    private String gender;

    private String alertTime;

    @NotBlank(message = "{can.not.be.blank}")
    private String status;
}
