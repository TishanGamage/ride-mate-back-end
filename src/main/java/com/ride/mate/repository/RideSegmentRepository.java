package com.ride.mate.repository;

import com.ride.mate.domain.RideSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Ride Segment Repository
 * Data access layer for ride_segment table
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
public interface RideSegmentRepository extends JpaRepository<RideSegment, Long> {

    /**
     * Find all segments for a ride, ordered by segment order
     *
     * @param rideDetailId Ride detail ID
     * @return Ordered list of ride segments
     */
    List<RideSegment> findByRideDetailIdOrderBySegmentOrder(Long rideDetailId);

    /**
     * Delete all segments for a ride (used during recalculation)
     *
     * @param rideDetailId Ride detail ID
     */
    void deleteByRideDetailId(Long rideDetailId);
}

