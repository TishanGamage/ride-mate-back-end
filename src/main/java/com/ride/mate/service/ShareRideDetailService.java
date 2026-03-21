package com.ride.mate.service;

import com.ride.mate.domain.ShareRideDetail;
import com.ride.mate.resources.ShareRideDetailAddResource;
import com.ride.mate.resources.ShareRideDetailResponse;
import com.ride.mate.resources.SharedRidePoolResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * Share Ride Detail Service Interface
 * Business logic for managing shared ride pooling and passenger ride requests
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Iruni           Initial Development
 * 2 21-03-2026    N/A          N/A          Tishan           Added passengerRideDistance to getAvailableRidePools;
 *                                                            removed searchNearbyRides and requestSharedRideWithMatching
 */
public interface ShareRideDetailService {

    /**
     * Join a shared ride — create a ShareRideDetail record for a passenger.
     * The passenger cost is calculated using max(60/N,20)% of their segment cost.
     */
    ShareRideDetail joinSharedRide(ShareRideDetailAddResource request);

    /**
     * Get available rides for pooling, ranked by ML acceptance probability.
     *
     * @param passengerStartLat     Passenger pickup latitude
     * @param passengerStartLng     Passenger pickup longitude
     * @param passengerEndLat       Passenger dropoff latitude
     * @param passengerEndLng       Passenger dropoff longitude
     * @param passengerRideDistance Passenger's route distance in km — used for accurate cost estimate
     * @param radiusKm              Search radius in km
     * @return ML-ranked list of available ride pools with estimated cost per passenger
     */
    List<SharedRidePoolResponse> getAvailableRidePools(
            BigDecimal passengerStartLat,
            BigDecimal passengerStartLng,
            BigDecimal passengerEndLat,
            BigDecimal passengerEndLng,
            BigDecimal passengerRideDistance,
            BigDecimal radiusKm
    );

    /** Get all passengers on a ride. */
    List<ShareRideDetailResponse> getRidePassengers(Long rideDetailId);

    /** Get ride history for a passenger. */
    List<ShareRideDetailResponse> getPassengerRideHistory(Long userId);

    /** Update a shared ride's status (CONFIRMED, COMPLETED, CANCELLED). */
    ShareRideDetail updateShareRideStatus(Long shareRideDetailId, String status);

    /** Cancel a shared ride by passenger. */
    ShareRideDetail cancelSharedRide(Long shareRideDetailId);

    /**
     * Calculate pooled cost for a ride using max(60/N,20)% algorithm.
     * Returns the cost per passenger based on current confirmed passenger count.
     */
    BigDecimal calculatePooledCost(Long rideDetailId);

    /** Get detailed info for a single shared-ride record. */
    ShareRideDetailResponse getSharedRideDetails(Long shareRideDetailId);
}
