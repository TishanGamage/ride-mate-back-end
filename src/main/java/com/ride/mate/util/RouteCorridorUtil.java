package com.ride.mate.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * Route Corridor Utility
 * Checks whether a given point lies within a corridor buffer along a polyline route.
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Tishan           Initial Development
 */
@Slf4j
public class RouteCorridorUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private RouteCorridorUtil() {}

    /**
     * Returns true if the given point (lat, lng) is within corridorKm of any segment
     * of the polyline encoded in tripRouteJson.
     *
     * tripRouteJson format: [[lat1,lng1],[lat2,lng2],...]
     */
    public static boolean isPointInCorridor(String tripRouteJson, BigDecimal pointLat,
                                            BigDecimal pointLng, double corridorKm) {
        if (tripRouteJson == null || tripRouteJson.isBlank() || pointLat == null || pointLng == null) {
            return false;
        }

        try {
            JsonNode root = MAPPER.readTree(tripRouteJson);
            if (!root.isArray() || root.size() < 2) return false;

            double pLat = pointLat.doubleValue();
            double pLng = pointLng.doubleValue();

            for (int i = 0; i < root.size() - 1; i++) {
                JsonNode a = root.get(i);
                JsonNode b = root.get(i + 1);
                double aLat = a.get(0).asDouble();
                double aLng = a.get(1).asDouble();
                double bLat = b.get(0).asDouble();
                double bLng = b.get(1).asDouble();

                double dist = pointToSegmentDistance(pLat, pLng, aLat, aLng, bLat, bLng);
                if (dist <= corridorKm) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse tripRoute JSON for corridor check: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Minimum distance (km) from point P to segment AB using Haversine.
     */
    private static double pointToSegmentDistance(double pLat, double pLng,
                                                  double aLat, double aLng,
                                                  double bLat, double bLng) {
        double abLat = bLat - aLat;
        double abLng = bLng - aLng;
        double apLat = pLat - aLat;
        double apLng = pLng - aLng;

        double ab2 = abLat * abLat + abLng * abLng;
        if (ab2 == 0) return haversine(pLat, pLng, aLat, aLng);

        double t = Math.max(0, Math.min(1, (apLat * abLat + apLng * abLng) / ab2));
        double closestLat = aLat + t * abLat;
        double closestLng = aLng + t * abLng;

        return haversine(pLat, pLng, closestLat, closestLng);
    }

    private static double haversine(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
