package com.ride.mate.resources;

import lombok.*;

import java.math.BigDecimal;

/**
 * Passenger Estimated Cost Response
 * Returns the estimated cost a passenger would pay for a specific ride
 * before submitting a ride request. Uses the max(60/N, 20)% algorithm
 * from costsplittinglogic.md.
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 21-03-2026    N/A          N/A          Tishan           Initial Development
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassengerEstimatedCostResponse {

    private Long rideDetailId;
    /** Current number of active passengers on the ride */
    private int currentPassengerCount;
    /** Passenger count after this passenger joins (N used in formula) */
    private int projectedPassengerCount;
    /** Per-km rate for this ride */
    private BigDecimal perKmRate;
    /** The passenger's route distance in km */
    private BigDecimal passengerRideDistance;
    /** Share percentage applied: max(60 / projectedPassengerCount, 20) */
    private BigDecimal sharePercentage;
    /** Estimated total cost for this passenger */
    private BigDecimal estimatedCost;
    /** Note shown to the passenger explaining the pricing model */
    private String pricingNote;
}

