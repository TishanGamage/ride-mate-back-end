package com.ride.mate.resources;

import lombok.*;

import java.math.BigDecimal;

/**
 * Driver Location Response Resource
 * DTO for returning driver's live location to passengers
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverLocationResponseResource {

    private Long rideDetailId;
    private Long driverProfileId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String lastUpdated;
    private String rideStatus;
    private Double bearing;
    private Long timestamp;
}

