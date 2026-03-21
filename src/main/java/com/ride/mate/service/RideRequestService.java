package com.ride.mate.service;

import com.ride.mate.resources.PassengerEstimatedCostResponse;
import com.ride.mate.resources.RideRequestResource;
import com.ride.mate.resources.RideRequestResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * Ride Request Service Interface
 * Business logic for managing ride requests between passengers and drivers.
 *
 * Ride discovery is handled by ShareRideDetailService.getAvailableRidePools()
 * which uses the ML service to rank available rides.
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Tishan           Initial Development
 * 2 21-03-2026    N/A          N/A          Tishan           Added cancelRideRequest and estimatePassengerCost
 * 3 21-03-2026    N/A          N/A          Tishan           Removed getAvailableRides (moved to ShareRideDetailService)
 */
public interface RideRequestService {

    /**
     * Get all available (ACTIVE) rides filtered by:
     * 1. Passenger's destination is within radiusKm of the driver's end point.
     * 2. Passenger's pickup point lies within the route corridor of the driver's trip.
     *
     * @param startLat Passenger's pickup latitude (optional)
     * @param startLng Passenger's pickup longitude (optional)
     * @param endLat   Passenger's destination latitude (optional)
     * @param endLng   Passenger's destination longitude (optional)
     * @param radiusKm Search radius in km (optional, default 15)
     * @return List of available rides
     */
    List<AvailableRideResponse> getAvailableRides(BigDecimal startLat, BigDecimal startLng,
                                                   BigDecimal endLat, BigDecimal endLng,
                                                   BigDecimal radiusKm);

    /**
     * Create a ride request (passenger requests to join a ride).
     */
    RideRequestResponse createRideRequest(RideRequestResource resource);

    /**
     * Get all pending requests for a driver's active rides.
     */
    List<RideRequestResponse> getPendingRequestsForDriver(Long driverProfileId);

    /**
     * Accept a ride request — creates the SharedRideDetail and recalculates cost.
     */
    RideRequestResponse acceptRideRequest(Long rideRequestId);

    /**
     * Reject a ride request.
     */
    RideRequestResponse rejectRideRequest(Long rideRequestId);

    /**
     * Cancel a ride request (passenger withdraws a PENDING or ACCEPTED request).
     * If the request was ACCEPTED, removes from ShareRideDetail and recalculates cost.
     */
    RideRequestResponse cancelRideRequest(Long rideRequestId);

    /**
     * Get all ride requests for a passenger.
     */
    List<RideRequestResponse> getRequestsByPassenger(Long userId);

    /**
     * Estimate the cost a passenger would pay for a specific ride before requesting.
     * Uses max(60/N, 20)% algorithm where N = currentPassengers + 1.
     *
     * @param rideDetailId          The target ride
     * @param passengerRideDistance The passenger's route distance in km
     * @return PassengerEstimatedCostResponse with estimated cost and pricing breakdown
     */
    PassengerEstimatedCostResponse estimatePassengerCost(Long rideDetailId, BigDecimal passengerRideDistance);
}





