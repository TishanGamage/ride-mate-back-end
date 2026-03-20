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
 */
public interface ShareRideDetailService {

    /**
     * Join a shared ride - Create a new shared ride detail record for a passenger
     *
     * @param request Share ride detail request resource
     * @return Created ShareRideDetail entity
     */
    ShareRideDetail joinSharedRide(ShareRideDetailAddResource request);

    /**
     * Get available rides for pooling based on passenger location and destination
     * Uses ML model to rank drivers by acceptance rate
     *
     * @param passengerStartLat Passenger start latitude
     * @param passengerStartLng Passenger start longitude
     * @param passengerEndLat Passenger end latitude
     * @param passengerEndLng Passenger end longitude
     * @param radiusKm Search radius in kilometers
     * @return List of available ride pools with matched passengers
     */
    List<SharedRidePoolResponse> getAvailableRidePools(
            BigDecimal passengerStartLat,
            BigDecimal passengerStartLng,
            BigDecimal passengerEndLat,
            BigDecimal passengerEndLng,
            BigDecimal radiusKm
    );

    /**
     * Get shared ride details for a specific ride (all passengers)
     *
     * @param rideDetailId Ride detail ID
     * @return List of shared ride details
     */
    List<ShareRideDetailResponse> getRidePassengers(Long rideDetailId);

    /**
     * Get shared ride history for a passenger
     *
     * @param userId User ID
     * @return List of shared ride details for the passenger
     */
    List<ShareRideDetailResponse> getPassengerRideHistory(Long userId);

    /**
     * Update shared ride status
     *
     * @param shareRideDetailId Shared ride detail ID
     * @param status New status (CONFIRMED, COMPLETED, CANCELLED)
     * @return Updated ShareRideDetail
     */
    ShareRideDetail updateShareRideStatus(Long shareRideDetailId, String status);

    /**
     * Cancel shared ride by passenger
     *
     * @param shareRideDetailId Shared ride detail ID
     * @return Updated ShareRideDetail with CANCELLED status
     */
    ShareRideDetail cancelSharedRide(Long shareRideDetailId);

    /**
     * Calculate pooled cost for passengers
     * Returns cost split among all passengers in the ride
     *
     * @param rideDetailId Ride detail ID
     * @return Cost per passenger after split
     */
    BigDecimal calculatePooledCost(Long rideDetailId);

    /**
     * Get ride details with all passengers and pooled cost information
     *
     * @param shareRideDetailId Shared ride detail ID
     * @return Complete ride details with cost information
     */
    ShareRideDetailResponse getSharedRideDetails(Long shareRideDetailId);

    /**
     * Request to join a shared ride with matching (calls ML service for driver acceptance prediction)
     *
     * @param request Share ride request with matching parameters
     * @return Created ShareRideDetail after ML-based matching
     */
    ShareRideDetail requestSharedRideWithMatching(ShareRideDetailAddResource request);

    /**
     * Get available shared rides near passenger location
     *
     * @param passengerStartLat Start latitude
     * @param passengerStartLng Start longitude
     * @param passengerEndLat End latitude
     * @param passengerEndLng End longitude
     * @return List of available ride pools
     */
    List<SharedRidePoolResponse> searchNearbyRides(
            BigDecimal passengerStartLat,
            BigDecimal passengerStartLng,
            BigDecimal passengerEndLat,
            BigDecimal passengerEndLng
    );
}
