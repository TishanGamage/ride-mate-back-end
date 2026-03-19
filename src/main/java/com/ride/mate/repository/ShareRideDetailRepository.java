package com.ride.mate.repository;

import com.ride.mate.domain.ShareRideDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Share Ride Detail Repository
 * Data access layer for shared_ride_detail table
 *
 * @author Dulan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 19-03-2026    N/A          N/A          Dulan           Initial Development
 */
@Repository
public interface ShareRideDetailRepository extends JpaRepository<ShareRideDetail, Long> {

    boolean existsByRideDetailIdAndUserId(Long rideDetailId, Long userId);

    long countByRideDetailIdAndStatus(Long rideDetailId, String status);
}
