package com.ride.mate.domain;

import com.ride.mate.core.BaseEntity;
import com.ride.mate.enums.RideSegmentStatus;
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
 * Segment types:
 *   MAIN      — the driver's regular commute route; cost split uses max(60/N, 20)%
 *   SIDE_TRIP — a detour off the main route to pick up / drop off a specific passenger;
 *               that passenger pays 60%, driver absorbs 40%.
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Tishan           Initial Development
 * 2 21-03-2026    N/A          N/A          Tishan           Added segmentType and sharePercentage fields
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

    /**
     * MAIN — driver's normal commute segment.
     * SIDE_TRIP — detour for a specific passenger pickup/dropoff.
     */
    @Column(name = "segment_type", nullable = false, length = 20)
    private String segmentType;

    /**
     * The percentage of this segment's cost that each passenger on this segment pays.
     * For MAIN segments: max(60 / N, 20) where N = number of passengers.
     * For SIDE_TRIP segments: always 60 (the benefiting passenger pays 60%).
     */
    @Column(name = "share_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal sharePercentage;

    @Column(name = "created_date", nullable = false)
    private Timestamp createdDate;

    @Column(name = "created_user", nullable = false, length = 100)
    private String createdUser;

    @Column(name = "modified_date")
    private Timestamp modifiedDate;

    @Column(name = "modified_user", length = 100)
    private String modifiedUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RideSegmentStatus status = RideSegmentStatus.ACTIVE;
}
