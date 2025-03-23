package com.ride.mate.resources;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Driver Location Update Resource
 * DTO for updating driver's live location during an active ride
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 01-04-2026    N/A          N/A          Tishan          Initial Development
 */
@Getter
@Setter
public class DriverLocationUpdateResource {

    @NotNull(message = "{invalid.value}")
    private BigDecimal latitude;

    @NotNull(message = "{invalid.value}")
    private BigDecimal longitude;
}

