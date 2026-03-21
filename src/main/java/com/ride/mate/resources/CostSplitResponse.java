package com.ride.mate.resources;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Cost Split Response
 * Complete response containing the cost breakdown for a shared ride.
 *
 * Algorithm (from costsplittinglogic.md):
 *   - MAIN segment with N passengers: each passenger pays max(60/N, 20)% of segment cost
 *   - SIDE_TRIP segment (detour for one passenger): that passenger pays 60%, driver absorbs 40%
 *   - Driver alone segment: no charge to anyone
 *
 * @author Tishan
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
        /** MAIN or SIDE_TRIP */
        private String segmentType;
        /** Percentage each passenger on this segment pays (e.g. 60.00, 40.00, 30.00) */
        private BigDecimal sharePercentage;
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
        /** MAIN or SIDE_TRIP */
        private String segmentType;
        /** Share percentage applied for this passenger on this segment */
        private BigDecimal sharePercentage;
        private BigDecimal passengerShareForSegment;
    }
}

