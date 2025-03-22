package com.ride.mate.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Admin Ride Response
 * Response payload for ride details in admin panel
 *
 * @author RideMate
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 02-04-2026    N/A          N/A          RideMate         Initial Development
 */
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminRideResponse {

    private Long id;
    private Long driverProfileId;
    private String driverName;
    private String driverEmail;
    private String startCity;
    private String endCity;
    private String startLocationLatitude;
    private String startLocationLongitude;
    private String endLocationLatitude;
    private String endLocationLongitude;
    private Long availableSeats;
    private String totalRideDistance;
    private String totalRideCost;
    private String perKmRate;
    private String status;
    private String startTime;
    private String createdDate;
}

