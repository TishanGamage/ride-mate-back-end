package com.ride.mate.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * RedisLocationUtil
 * Utility for storing and retrieving driver location in Redis
 *
 * @author GitHub Copilot
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 21-03-2026    N/A          N/A          GitHub Copilot   Initial Development
 */
@Component
public class RedisLocationUtil {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisLocationUtil(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setDriverLocation(Long rideDetailId, BigDecimal latitude, BigDecimal longitude) {
        String key = getLocationKey(rideDetailId);
        String value = latitude + "," + longitude;
        redisTemplate.opsForValue().set(key, value);
    }

    public BigDecimal[] getDriverLocation(Long rideDetailId) {
        String key = getLocationKey(rideDetailId);
        String value = redisTemplate.opsForValue().get(key);
        if (value == null || !value.contains(",")) {
            return null;
        }
        String[] parts = value.split(",");
        return new BigDecimal[]{new BigDecimal(parts[0]), new BigDecimal(parts[1])};
    }

    private String getLocationKey(Long rideDetailId) {
        return "ride:" + rideDetailId + ":driver_location";
    }
}

