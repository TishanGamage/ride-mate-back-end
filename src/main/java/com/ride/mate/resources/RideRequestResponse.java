package com.ride.mate.resources;

import lombok.*;

import java.math.BigDecimal;

/**
 * Ride Request Response DTO
 * Returns ride request details with passenger information
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Tishan           Initial Development
 * 2 21-03-2026    N/A          N/A          Tishan           Added estimatedCost field
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideRequestResponse {

    private Long id;
    private Long rideDetailId;
    private Long userId;
    private String passengerFirstName;
    private String passengerLastName;
    private String passengerEmail;
    private String passengerPhone;
    private String passengerProfileImageUrl;
    private BigDecimal passengerStartLat;
    private BigDecimal passengerStartLng;
    private BigDecimal passengerEndLat;
    private BigDecimal passengerEndLng;
    private String startCity;
    private String endCity;
    private BigDecimal passengerRideDistance;
    /** Estimated cost for this passenger (calculated on accept using max(60/N,20)% algorithm) */
    private BigDecimal estimatedCost;
    private String status;
    private String createdDate;
}

