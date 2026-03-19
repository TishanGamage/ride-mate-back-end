package com.ride.mate.service;

import com.ride.mate.resources.CostSplitResponse;

/**
 * Cost Split Service Interface
 * Handles segment-based cost splitting for ride-sharing.
 *
 * Algorithm:
 * 1. Collect all waypoints along the driver's route (driver start, passenger pickups/dropoffs, driver end)
 * 2. Order waypoints by their position along the route
 * 3. Divide the route into segments between consecutive waypoints
 * 4. For each segment, determine how many riders (driver + passengers) are on that segment
 * 5. Split each segment's cost equally among riders present
 * 6. Each passenger's total cost = sum of their share across all segments they ride
 * 7. Driver's effective cost = total ride cost - sum of all passenger payments
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Tishan           Initial Development
 */
public interface CostSplitService {

    /**
     * Calculate and persist the cost split for a ride.
     * Called whenever the passenger list changes (join/cancel).
     *
     * @param rideDetailId The ride to recalculate
     * @return CostSplitResponse with full breakdown
     */
    CostSplitResponse calculateCostSplit(Long rideDetailId);

    /**
     * Get the current cost split for a ride (reads from persisted segments).
     * If no segments exist yet, calculates them first.
     *
     * @param rideDetailId The ride detail ID
     * @return CostSplitResponse with full breakdown
     */
    CostSplitResponse getCostSplit(Long rideDetailId);
}

