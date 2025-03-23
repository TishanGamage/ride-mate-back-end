package com.ride.mate.controller;
import com.ride.mate.resources.DriverLocationResponseResource;
import com.ride.mate.service.DriverLocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.math.BigDecimal;
import java.util.Map;
/**
 * WebSocket Controller for Driver Live Location
 *
 * STOMP message flow:
 *   Driver sends  ->  /app/ride/{rideId}/location   (this controller receives)
 *   Server caches in Redis, publishes to Redis pub/sub
 *   Redis subscriber broadcasts  ->  /topic/ride/{rideId}/location  (passengers receive)
 *
 * REST endpoint:
 *   GET /ride-details/{rideDetailId}/driver-location
 *   Returns the latest cached location from Redis (for initial load before WS connects)
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Controller
@RequestMapping("/ride-details")
public class DriverLocationWebSocketController {
    private final DriverLocationService driverLocationService;
    public DriverLocationWebSocketController(DriverLocationService driverLocationService) {
        this.driverLocationService = driverLocationService;
    }
    // --- STOMP Message Handler -------------------------------------
    /**
     * Receives driver location via STOMP WebSocket.
     * Client sends to: /app/ride/{rideId}/location
     *
     * Expected JSON payload:
     * {
     *   "rideId": 42,
     *   "latitude": 6.9271,
     *   "longitude": 79.8612,
     *   "bearing": 45.0,
     *   "timestamp": 1711929600000
     * }
     */
    @MessageMapping("/ride/{rideId}/location")
    public void handleDriverLocation(
            @DestinationVariable Long rideId,
            @Payload Map<String, Object> payload) {
        try {
            BigDecimal latitude = toBigDecimal(payload.get("latitude"));
            BigDecimal longitude = toBigDecimal(payload.get("longitude"));
            double bearing = toDouble(payload.get("bearing"), 0.0);
            long timestamp = toLong(payload.get("timestamp"), System.currentTimeMillis());
            if (latitude == null || longitude == null) {
                log.warn("[WS Location] Invalid payload for ride #{}: missing lat/lng", rideId);
                return;
            }
            driverLocationService.updateDriverLocation(
                    rideId, latitude, longitude, bearing, timestamp);
        } catch (Exception e) {
            log.error("[WS Location] Error processing location for ride #{}: {}",
                    rideId, e.getMessage());
        }
    }
    // --- REST Endpoint (initial load) ------------------------------
    /**
     * GET /ride-details/{rideDetailId}/driver-location
     * Returns the latest cached driver location from Redis.
     * Used by the passenger app to get the driver position before WebSocket connects.
     */
    @GetMapping("/{rideDetailId}/driver-location")
    @ResponseBody
    public ResponseEntity<?> getDriverLocation(@PathVariable Long rideDetailId) {
        log.info("[REST] Get driver location for ride #{}", rideDetailId);
        DriverLocationResponseResource location =
                driverLocationService.getLatestDriverLocation(rideDetailId);
        if (location == null) {
            return new ResponseEntity<>(
                    Map.of("message", "No cached location available"),
                    HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(location, HttpStatus.OK);
    }
    // --- Helpers ---------------------------------------------------
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    private double toDouble(Object value, double defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    private long toLong(Object value, long defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Number) return ((Number) value).longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
