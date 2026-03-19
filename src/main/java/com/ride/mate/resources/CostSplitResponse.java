package com.ride.mate.resources;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Cost Split Response
 * Complete response containing segment-based cost breakdown for a ride.
 *
 * The algorithm divides the route into segments based on all pickup/dropoff
 * waypoints. Each segment's cost is split equally among all riders present
 * on that segment (driver + passengers who are on-board).
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
public class CostSplitResponse {

    private Long rideDetailId;
    private BigDecimal totalRideCost;
    private BigDecimal totalRideDistance;
    private BigDecimal perKmRate;
    private BigDecimal driverEffectiveCost;
    private String driverStartCity;
    private int totalPassengers;

    private List<SegmentDetail> segments;
    private List<PassengerCostDetail> passengerCosts;

    /**
     * Represents one segment between two consecutive waypoints on the route.
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SegmentDetail {
        private int segmentOrder;
        private String startLabel;
        private String endLabel;
        private BigDecimal distanceKm;
        private int riderCount;
        private BigDecimal segmentCost;
        private BigDecimal costPerRider;
    }

    /**
     * Represents the cost breakdown for a single passenger.
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PassengerCostDetail {
        private Long userId;
        private Long shareRideDetailId;
        private String startCity;
        private String endCity;
        private BigDecimal passengerRideDistance;
        private BigDecimal totalPassengerCost;
        private List<PassengerSegmentCost> segmentBreakdown;
    }

    /**
     * Individual segment cost for a passenger.
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PassengerSegmentCost {
        private int segmentOrder;
        private String startLabel;
        private String endLabel;
        private BigDecimal distanceKm;
        private int riderCount;
        private BigDecimal passengerShareForSegment;
    }
}

