package com.ride.mate.resources;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideDetailResponseResource {

    private Long id;
    private Long driverProfileId;
    private BigDecimal startLocationLongitude;
    private BigDecimal startLocationLatitude;
    private BigDecimal endLocationLongitude;
    private BigDecimal endLocationLatitude;
    private String startCity;
    private String endCity;
    private Long availableSeats;
    private String startTime;
    private BigDecimal totalRideDistance;
    private BigDecimal totalRideCost;
    private BigDecimal perKmRate;
    private String tripRoute;
    private String status;
    private String createdDate;
    private BigDecimal currentLocationLatitude;
    private BigDecimal currentLocationLongitude;
}
