package com.ride.mate.repository;

import com.ride.mate.domain.ShareRideDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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
 * 2 20-03-2026    N/A          N/A          Iruni            Added additional query methods for pooling
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
     * Find shared ride details by user ID and status
     *
     * @param userId User ID
     * @param status Status filter
     * @return List of shared ride details for the user with specific status
     */
    List<ShareRideDetail> findByUserIdAndStatus(Long userId, String status);

    /**
     * Count passengers for a ride
     *
     * @param rideDetailId Ride detail ID
     * @param status Status filter
     * @return Number of passengers
     */
    long countByRideDetailIdAndStatus(Long rideDetailId, String status);

    /**
     * Find shared rides within a geographic region
     *
     * @param minLat Minimum latitude
     * @param maxLat Maximum latitude
     * @param minLng Minimum longitude
     * @param maxLng Maximum longitude
     * @param status Status filter
     * @return List of shared rides in the region
     */
    @Query("SELECT s FROM ShareRideDetail s WHERE " +
           "s.startLocationLatitude BETWEEN :minLat AND :maxLat AND " +
           "s.startLocationLongitude BETWEEN :minLng AND :maxLng AND " +
           "s.status = :status")
    List<ShareRideDetail> findByGeographicRegion(
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat,
            @Param("minLng") BigDecimal minLng,
            @Param("maxLng") BigDecimal maxLng,
            @Param("status") String status);

    /**
     * Find available shared rides going from one city to another
     *
     * @param startCity Start city name
     * @param endCity End city name
     * @param status Status filter
     * @return List of available shared rides with matching route
     */
    List<ShareRideDetail> findByStartCityAndEndCityAndStatus(
            String startCity, String endCity, String status);

    /**
     * Count total shared rides for a passenger
     *
     * @param userId User ID
     * @return Total number of shared rides for the passenger
     */
    long countByUserId(Long userId);

    /**
     * Find active shared rides for a passenger
     *
     * @param userId User ID
     * @return List of active/pending shared rides
     */
    List<ShareRideDetail> findByUserIdAndStatusIn(Long userId, List<String> statuses);
}

