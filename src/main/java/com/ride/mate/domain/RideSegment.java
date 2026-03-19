package com.ride.mate.domain;

import com.ride.mate.core.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Ride Segment Entity
 * Stores individual segments of a ride for cost-splitting calculations.
 * Each segment represents the distance between two consecutive waypoints
 * (driver start, passenger pickups, passenger dropoffs, driver end).
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Tishan           Initial Development
 */
@Getter
@Setter
@Entity
@Table(name = "ride_segment")
public class RideSegment extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_detail_id", nullable = false)
    private RideDetail rideDetail;

    @Column(name = "segment_order", nullable = false)
    private Integer segmentOrder;

    @Column(name = "start_latitude", precision = 10, scale = 7, nullable = false)
    private BigDecimal startLatitude;

    @Column(name = "start_longitude", precision = 10, scale = 7, nullable = false)
    private BigDecimal startLongitude;

    @Column(name = "end_latitude", precision = 10, scale = 7, nullable = false)
    private BigDecimal endLatitude;

    @Column(name = "end_longitude", precision = 10, scale = 7, nullable = false)
    private BigDecimal endLongitude;

    @Column(name = "start_label", length = 200)
    private String startLabel;

    @Column(name = "end_label", length = 200)
    private String endLabel;

    @Column(name = "distance_km", precision = 10, scale = 2, nullable = false)
    private BigDecimal distanceKm;

    @Column(name = "rider_count", nullable = false)
    private Integer riderCount;

    @Column(name = "segment_cost", precision = 25, scale = 2, nullable = false)
    private BigDecimal segmentCost;

    @Column(name = "cost_per_rider", precision = 25, scale = 2, nullable = false)
    private BigDecimal costPerRider;

    @Column(name = "created_date", nullable = false)
    private Timestamp createdDate;

    @Column(name = "created_user", nullable = false, length = 100)
    private String createdUser;

    @Column(name = "modified_date")
    private Timestamp modifiedDate;

    @Column(name = "modified_user", length = 100)
    private String modifiedUser;
}

