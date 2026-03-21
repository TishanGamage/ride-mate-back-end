package com.ride.mate.service;

import com.ride.mate.resources.CostSplitResponse;

/**
 * Cost Split Service Interface
 * Handles segment-based cost splitting for ride-sharing.
 *
 * Algorithm (daily commuter ride-share model):
 *  The driver is already travelling this route — passengers pay a fair share of running
 *  costs, not a full private-hire fee.
 *
 *  MAIN segments (driver's regular route):
 *    - N passengers in the car → each pays max(60 / N, 20)% of the segment cost
 *    - N=1 → 60%, N=2 → 40%, N=3 → 30%, N=4 → 25%, N≥5 → 20% (floor)
 *    - Driver alone → no charge to anyone
 *
 *  SIDE_TRIP segments (detour off the main route for one passenger):
 *    - That passenger pays 60% of the detour cost
 *    - Driver absorbs 40% (goodwill for the detour)
 *    - No other passenger shares the detour cost
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Tishan           Initial Development
 * 2 21-03-2026    N/A          N/A          Tishan           Updated algorithm to max(60/N,20)% model
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

