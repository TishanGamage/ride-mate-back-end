package com.ride.mate.domain;

import com.ride.mate.core.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Ride Request Entity
 * Stores ride join requests from passengers to drivers
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Tishan           Initial Development
 * 2 22-03-2026    N/A          N/A          Tishan           Added estimatedCost field
 */
@Getter
@Setter
@Entity
@Table(name = "ride_request")
public class RideRequest extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_detail_id", nullable = false)
    private RideDetail rideDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "passenger_start_lat", precision = 10, scale = 7)
    private BigDecimal passengerStartLat;

    @Column(name = "passenger_start_lng", precision = 10, scale = 7)
    private BigDecimal passengerStartLng;

    @Column(name = "passenger_end_lat", precision = 10, scale = 7)
    private BigDecimal passengerEndLat;

    @Column(name = "passenger_end_lng", precision = 10, scale = 7)
    private BigDecimal passengerEndLng;

    @Column(name = "start_city", length = 200)
    private String startCity;

    @Column(name = "end_city", length = 200)
    private String endCity;

    @Column(name = "passenger_ride_distance", precision = 25, scale = 2)
    private BigDecimal passengerRideDistance;

    @Column(name = "estimated_cost", precision = 25, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "status", nullable = false, length = 20)
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

