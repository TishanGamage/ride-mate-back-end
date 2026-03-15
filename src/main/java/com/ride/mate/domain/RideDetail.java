package com.ride.mate.domain;

import com.ride.mate.core.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Ride Detail Entity
 * Stores ride information posted by drivers for ride-sharing
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 14-03-2026    N/A          N/A          Iruni           Initial Development
 */
@Getter
@Setter
@Entity
@Table(name = "driver_ride_detail")
public class RideDetail extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_profile_id", nullable = false)
    private DriverProfile driverProfile;

    @Column(name = "start_location_longitude", precision = 10, scale = 7)
    private BigDecimal startLocationLongitude = BigDecimal.ZERO;

    @Column(name = "end_location_longitude", precision = 10, scale = 7)
    private BigDecimal endLocationLongitude = BigDecimal.ZERO ;

    @Column(name = "start_city", length = 100)
    private String startCity;

    @Column(name = "available_seats")
    private Long availableSeats;

    @Column(name = "start_time")
    private Timestamp startTime;

    @Column(name = "total_ride_distance", precision = 10, scale = 2)
    private BigDecimal totalRideDistance = BigDecimal.ZERO;

    @Lob
    @Column(name = "trip_route")
    private String tripRoute;

    @Column(name = "alert_time")
    private Timestamp alertTime;

    @Column(name = "status", length = 100)
    private String status;

    @Column(name = "created_date", nullable = false)
    private Timestamp createdDate;

    @Column(name = "created_user", nullable = false, length = 100)
    private String createdUser;

    @Column(name = "modified_date")
    private Timestamp modifiedDate;

    @Column(name = "modified_user", length = 100)
    private String modifiedUser;
}
