package com.ride.mate.resources;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Passenger Ride Confirm Response
 * Response payload after a passenger confirms a ride
 *
 * @author Dulan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 19-03-2026    N/A          N/A          Dulan           Initial Development
 */
@Getter
@Setter
@Builder
public class PassengerRideConfirmResponse {

    private Long shareRideDetailId;
    private Long rideDetailId;
    private Long userId;
    private BigDecimal passengerCost;
    private BigDecimal passengerRideDistance;
    private String startCity;
    private String endCity;
    private String status;
    private String message;
}
