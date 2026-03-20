package com.ride.mate.service;

import com.ride.mate.domain.RideDetail;
import com.ride.mate.resources.CostSplitResponse;
import com.ride.mate.resources.PassengerRideConfirmRequestResource;
import com.ride.mate.resources.RideDetailRequestResource;
import com.ride.mate.resources.RidePriceCalculationResponse;

import java.math.BigDecimal;

/**
 * Ride Detail Service Interface
 * Business logic for managing ride details
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 15-03-2026    N/A          N/A          Iruni           Initial Development
 * 2 19-03-2026    N/A          N/A          Iruni           Added calculateRidePrice method
 * 3 20-03-2026    N/A          N/A          Tishan           Added confirmPassengerRide method
 */
public interface RideDetailService {

    /**
     * Create a new ride detail
     *
     * @param request Ride detail request resource
     * @return Created RideDetail entity
     */
    RideDetail createRideDetail(RideDetailRequestResource request);

    /**
     * Calculate ride price based on total distance and driver profile
     * Algorithm: Fetch driver vehicle details -> Get vehicle type -> Get per km rate -> Calculate price
     *
     * @param driverProfileId Driver profile ID
     * @param totalDistance Total distance in kilometers
     * @return RidePriceCalculationResponse with calculated price details
     */
    RidePriceCalculationResponse calculateRidePrice(Long driverProfileId, BigDecimal totalDistance);

    /**
     * Confirm a passenger joining a ride.
     * Creates a ShareRideDetail record and recalculates the cost split.
     *
     * @param request Passenger ride confirm request
     * @return CostSplitResponse with updated cost breakdown
     */
    CostSplitResponse confirmPassengerRide(PassengerRideConfirmRequestResource request);

    /**
     * End an active ride by updating its status to COMPLETED
     *
     * @param rideDetailId Ride detail ID
     * @return Updated RideDetail entity
     */
    RideDetail endRide(Long rideDetailId);

    /**
     * Get active ride for a driver profile
     *
     * @param driverProfileId Driver profile ID
     * @return Active RideDetail if exists
     */
    RideDetail getActiveRideByDriverProfileId(Long driverProfileId);
}

