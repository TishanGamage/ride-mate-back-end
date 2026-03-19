package com.ride.mate.repository;

import com.ride.mate.domain.ShareRideDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Share Ride Detail Repository
 * Data access layer for shared_ride_detail table
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Tishan           Initial Development
 */
@Repository
public interface ShareRideDetailRepository extends JpaRepository<ShareRideDetail, Long> {

    /**
     * Find all shared ride details for a specific ride
     *
     * @param rideDetailId Ride detail ID
     * @return List of shared ride details (passengers)
     */
    List<ShareRideDetail> findByRideDetailId(Long rideDetailId);

    /**
     * Find all active shared ride details for a specific ride
     *
     * @param rideDetailId Ride detail ID
     * @param status Status filter
     * @return List of active shared ride details
     */
    List<ShareRideDetail> findByRideDetailIdAndStatus(Long rideDetailId, String status);

    /**
     * Find shared ride details by user ID
     *
     * @param userId User ID
     * @return List of shared ride details for the user
     */
    List<ShareRideDetail> findByUserId(Long userId);

    /**
     * Count passengers for a ride
     *
     * @param rideDetailId Ride detail ID
     * @param status Status filter
     * @return Number of passengers
     */
    long countByRideDetailIdAndStatus(Long rideDetailId, String status);
}

