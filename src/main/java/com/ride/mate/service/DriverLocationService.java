package com.ride.mate.service;
import com.ride.mate.resources.DriverLocationResponseResource;
import java.math.BigDecimal;
/**
 * Driver Location Service Interface
 * Handles real-time driver location caching (Redis) and pub/sub broadcasting.
 *
 * Redis is used ONLY for live location tracking.
 * MySQL remains the primary database for all other data.
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 */
public interface DriverLocationService {
    /**
     * Cache driver location in Redis and publish to Redis pub/sub channel.
     *
     * @param rideId    Ride detail ID
     * @param latitude  Driver latitude
     * @param longitude Driver longitude
     * @param bearing   Driver bearing/heading in degrees
     * @param timestamp Epoch millis of the update
     */
    void updateDriverLocation(Long rideId, BigDecimal latitude, BigDecimal longitude,
                               double bearing, long timestamp);
    /**
     * Get the latest cached driver location from Redis.
     *
     * @param rideId Ride detail ID
     * @return DriverLocationResponseResource or null if no cached location
     */
    DriverLocationResponseResource getLatestDriverLocation(Long rideId);
    /**
     * Remove cached driver location from Redis (e.g. when ride ends).
     *
     * @param rideId Ride detail ID
     */
    void clearDriverLocation(Long rideId);
}
