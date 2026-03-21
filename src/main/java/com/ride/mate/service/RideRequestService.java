package com.ride.mate.service;

import com.ride.mate.resources.AvailableRideResponse;
import com.ride.mate.resources.RideRequestResource;
import com.ride.mate.resources.RideRequestResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * Ride Request Service Interface
 * Business logic for managing ride requests between passengers and drivers
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Tishan           Initial Development
 */
public interface RideRequestService {

    /**
     * Get all available (ACTIVE) rides, optionally filtered near the passenger's destination.
     *
     * @param endLat  Passenger's destination latitude (optional)
     * @param endLng  Passenger's destination longitude (optional)
     * @param radiusKm Search radius in km (optional, default 15)
     * @return List of available rides
     */
    List<AvailableRideResponse> getAvailableRides(BigDecimal endLat, BigDecimal endLng, BigDecimal radiusKm);

    /**
     * Create a ride request (passenger requests to join a ride).
     *
     * @param resource Ride request details
     * @return Created ride request response
     */
    RideRequestResponse createRideRequest(RideRequestResource resource);

    /**
     * Get all pending requests for a driver's active rides.
     *
     * @param driverProfileId Driver profile ID
     * @return List of pending ride requests
     */
    List<RideRequestResponse> getPendingRequestsForDriver(Long driverProfileId);

    /**
     * Accept a ride request — creates the SharedRideDetail and recalculates cost.
     *
     * @param rideRequestId Ride request ID
     * @return Updated ride request response
     */
    RideRequestResponse acceptRideRequest(Long rideRequestId);

    /**
     * Reject a ride request.
     *
     * @param rideRequestId Ride request ID
     * @return Updated ride request response
     */
    RideRequestResponse rejectRideRequest(Long rideRequestId);

    /**
     * Get all ride requests for a passenger.
     *
     * @param userId User ID
     * @return List of ride requests
     */
    List<RideRequestResponse> getRequestsByPassenger(Long userId);
}

