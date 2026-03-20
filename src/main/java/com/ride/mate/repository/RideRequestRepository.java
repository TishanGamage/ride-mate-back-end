package com.ride.mate.repository;

import com.ride.mate.domain.RideRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Ride Request Repository
 * Data access layer for ride_request table
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Tishan           Initial Development
 */
@Repository
public interface RideRequestRepository extends JpaRepository<RideRequest, Long> {

    List<RideRequest> findByRideDetailId(Long rideDetailId);

    List<RideRequest> findByRideDetailIdAndStatus(Long rideDetailId, String status);

    List<RideRequest> findByUserId(Long userId);

    List<RideRequest> findByUserIdAndStatus(Long userId, String status);

    boolean existsByRideDetailIdAndUserIdAndStatusIn(Long rideDetailId, Long userId, List<String> statuses);

    /**
     * Find all pending requests for rides belonging to a specific driver
     */
    @Query("SELECT rr FROM RideRequest rr " +
           "JOIN rr.rideDetail rd " +
           "WHERE rd.driverProfile.id = :driverProfileId " +
           "AND rr.status = :status " +
           "ORDER BY rr.createdDate DESC")
    List<RideRequest> findByDriverProfileIdAndStatus(
            @Param("driverProfileId") Long driverProfileId,
            @Param("status") String status);
}

