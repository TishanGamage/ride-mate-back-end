package com.ride.mate.domain;

import com.ride.mate.core.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Share Ride Detail Entity
 * Stores information about passengers sharing a ride posted by drivers
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
@Table(name = "shared_ride_detail")
public class ShareRideDetail extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_detail_id", nullable = false, unique = true)
    private RideDetail rideDetail;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "passenger_start_location_longitude", precision = 10, scale = 7)
    private BigDecimal startLocationLongitude = BigDecimal.ZERO;

    @Column(name = "passenger_end_location_longitude", precision = 10, scale = 7)
    private BigDecimal endLocationLongitude = BigDecimal.ZERO ;

    @Column(name = "passenger_cost", precision = 25, scale = 2, nullable = false)
    private BigDecimal passengerCost;

    @Column(name = "passenger_ride_distance", precision = 25, scale = 2, nullable = false)
    private BigDecimal passengerRideDistance;

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
