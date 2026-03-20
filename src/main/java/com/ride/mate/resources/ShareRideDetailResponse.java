package com.ride.mate.resources;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Share Ride Detail Response (DTO)
 * Response payload with shared ride passenger details
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
public class ShareRideDetailResponse {

    private Long id;

    private Long rideDetailId;

    private Long userId;

    private String userEmail;

    private String passengerName;

    private BigDecimal passengerStartLat;

    private BigDecimal passengerStartLng;

    private BigDecimal passengerEndLat;

    private BigDecimal passengerEndLng;

    private String startCity;

    private String endCity;

    private BigDecimal passengerRideDistance;

    private BigDecimal passengerCost;

    private String status;

    private Timestamp createdDate;

    private Timestamp modifiedDate;
}
