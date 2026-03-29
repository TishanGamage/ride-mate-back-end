package com.ride.mate.repository;

import com.ride.mate.domain.RideDetail;
import com.ride.mate.enums.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Ride Detail Repository
 * Data access layer for driver_ride_detail table
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 15-03-2026    N/A          N/A          Iruni           Initial Development
 */
@Repository
public interface RideDetailRepository extends JpaRepository<RideDetail, Long> {

    /**
     * Find ride details by driver profile ID
     *
     * @param driverProfileId Driver profile ID
     * @return List of ride details
     */
    List<RideDetail> findByDriverProfileId(Long driverProfileId);

    /**
     * Find ride details by status
     *
     * @param status Status
     * @return List of ride details
     */
    List<RideDetail> findByStatus(RideStatus status);

    boolean existsRideDetailByDriverProfileIdAndStatus(Long driverProfileId , RideStatus status);

    List<RideDetail> findByDriverProfileIdAndStatus(Long driverProfileId, RideStatus status);

    long countByStatus(RideStatus status);
}
