package com.ride.mate.resources;

import lombok.*;

import java.math.BigDecimal;

/**
 * Available Ride Response DTO
 * Summary of an available ride for passengers browsing rides
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailableRideResponse {

    private Long rideDetailId;
    private String driverFirstName;
    private String driverLastName;
    private String driverGender;
    private String driverProfileImageUrl;
    private BigDecimal driverRating;
    private Long totalRidesAsDriver;
    private String vehicleTypeName;
    private String vehicleMakeName;
    private String vehicleModelName;
    private String vehicleColor;
    private String vehiclePlateNumber;
    private String startCity;
    private String endCity;
    private BigDecimal startLat;
    private BigDecimal startLng;
    private BigDecimal endLat;
    private BigDecimal endLng;
    private BigDecimal totalRideDistance;
    private BigDecimal totalRideCost;
    private BigDecimal perKmRate;
    private Long availableSeats;
    private Long currentPassengers;
    private String startTime;
    private String status;
}

