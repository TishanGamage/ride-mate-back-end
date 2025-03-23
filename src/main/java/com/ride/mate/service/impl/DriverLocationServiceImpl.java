package com.ride.mate.service.impl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ride.mate.config.RedisConfig;
import com.ride.mate.resources.DriverLocationResponseResource;
import com.ride.mate.service.DriverLocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
/**
 * Driver Location Service Implementation
 * Uses Redis Hash for caching and Redis Pub/Sub for broadcasting.
 *
 * Redis key pattern:  ride:{rideId}:driver-location
 * Hash fields:        latitude, longitude, bearing, timestamp, rideId
 * TTL:                30 minutes (auto-expire stale entries)
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Service
public class DriverLocationServiceImpl implements DriverLocationService {
    private static final String KEY_PREFIX = "ride:";
    private static final String KEY_SUFFIX = ":driver-location";
    private static final long TTL_MINUTES = 30;
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .withZone(ZoneId.of("UTC"));
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    public DriverLocationServiceImpl(RedisTemplate<String, String> redisTemplate,
                                      ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }
    @Override
    public void updateDriverLocation(Long rideId, BigDecimal latitude, BigDecimal longitude,
                                      double bearing, long timestamp) {
        String redisKey = buildKey(rideId);
        // 1. Cache in Redis Hash
        Map<String, String> locationData = new HashMap<>();
        locationData.put("rideId", rideId.toString());
        locationData.put("latitude", latitude.toPlainString());
        locationData.put("longitude", longitude.toPlainString());
        locationData.put("bearing", String.valueOf(bearing));
        locationData.put("timestamp", String.valueOf(timestamp));
        redisTemplate.opsForHash().putAll(redisKey, locationData);
        redisTemplate.expire(redisKey, TTL_MINUTES, TimeUnit.MINUTES);
        // 2. Publish to Redis pub/sub channel for multi-instance broadcast
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("rideId", rideId);
            payload.put("latitude", latitude);
            payload.put("longitude", longitude);
            payload.put("bearing", bearing);
            payload.put("timestamp", timestamp);
            String json = objectMapper.writeValueAsString(payload);
            redisTemplate.convertAndSend(RedisConfig.LOCATION_CHANNEL, json);
            log.debug("[DriverLocation] Updated & published ride #{}: ({}, {})",
                    rideId, latitude, longitude);
        } catch (Exception e) {
            log.error("[DriverLocation] Failed to publish location: {}", e.getMessage());
        }
    }
    @Override
    public DriverLocationResponseResource getLatestDriverLocation(Long rideId) {
        String redisKey = buildKey(rideId);
        Map<Object, Object> data = redisTemplate.opsForHash().entries(redisKey);
        if (data.isEmpty()) {
            log.debug("[DriverLocation] No cached location for ride #{}", rideId);
            return null;
        }
        try {
            String latStr = (String) data.get("latitude");
            String lngStr = (String) data.get("longitude");
            String tsStr = (String) data.get("timestamp");
            long ts = tsStr != null ? Long.parseLong(tsStr) : System.currentTimeMillis();
            String formattedTime = FORMATTER.format(Instant.ofEpochMilli(ts));
            return DriverLocationResponseResource.builder()
                    .rideDetailId(rideId)
                    .latitude(latStr != null ? new BigDecimal(latStr) : null)
                    .longitude(lngStr != null ? new BigDecimal(lngStr) : null)
                    .lastUpdated(formattedTime)
                    .rideStatus("ACTIVE")
                    .build();
        } catch (Exception e) {
            log.error("[DriverLocation] Error reading cached location for ride #{}: {}",
                    rideId, e.getMessage());
            return null;
        }
    }
    @Override
    public void clearDriverLocation(Long rideId) {
        String redisKey = buildKey(rideId);
        redisTemplate.delete(redisKey);
        log.debug("[DriverLocation] Cleared cached location for ride #{}", rideId);
    }
    private String buildKey(Long rideId) {
        return KEY_PREFIX + rideId + KEY_SUFFIX;
    }
}
