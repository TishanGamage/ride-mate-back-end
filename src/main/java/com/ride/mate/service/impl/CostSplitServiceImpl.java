package com.ride.mate.service.impl;

import com.ride.mate.core.LoginAuthentication;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.RideDetail;
import com.ride.mate.domain.RideSegment;
import com.ride.mate.domain.ShareRideDetail;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.RideDetailRepository;
import com.ride.mate.repository.RideSegmentRepository;
import com.ride.mate.repository.ShareRideDetailRepository;
import com.ride.mate.resources.CostSplitResponse;
import com.ride.mate.service.CostSplitService;
import com.ride.mate.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Cost Split Service Implementation
 *
 * Segment-based cost-splitting algorithm for ride-sharing:
 *
 * Example scenario:
 *   Driver: Gampaha → Colombo (30 km, Rs.50/km = Rs.1500 total)
 *   Route passes through: Gampaha → Kadawatha (10km) → Kiribathgoda (5km) → Colombo (15km)
 *   Passenger A: Kadawatha → Colombo (joins at Kadawatha)
 *   Passenger B: Kiribathgoda → Colombo (joins at Kiribathgoda)
 *
 *   Segment 1: Gampaha → Kadawatha (10km) — Driver only (1 rider)
 *     Cost = 10 × 50 = Rs.500, Driver pays Rs.500
 *
 *   Segment 2: Kadawatha → Kiribathgoda (5km) — Driver + Passenger A (2 riders)
 *     Cost = 5 × 50 = Rs.250, each pays Rs.125
 *
 *   Segment 3: Kiribathgoda → Colombo (15km) — Driver + Passenger A + Passenger B (3 riders)
 *     Cost = 15 × 50 = Rs.750, each pays Rs.250
 *
 *   Passenger A total: Rs.125 + Rs.250 = Rs.375
 *   Passenger B total: Rs.250
 *   Driver effective cost: Rs.1500 - Rs.375 - Rs.250 = Rs.875
 *     (or: Rs.500 + Rs.125 + Rs.250 = Rs.875)
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Service
@Transactional
public class CostSplitServiceImpl extends MessagePropertyBase implements CostSplitService {

    private final RideDetailRepository rideDetailRepository;
    private final ShareRideDetailRepository shareRideDetailRepository;
    private final RideSegmentRepository rideSegmentRepository;
    private final Environment environment;

    public CostSplitServiceImpl(RideDetailRepository rideDetailRepository,
                                ShareRideDetailRepository shareRideDetailRepository,
                                RideSegmentRepository rideSegmentRepository,
                                Environment environment) {
        this.rideDetailRepository = rideDetailRepository;
        this.shareRideDetailRepository = shareRideDetailRepository;
        this.rideSegmentRepository = rideSegmentRepository;
        this.environment = environment;
    }

    @Override
    public CostSplitResponse calculateCostSplit(Long rideDetailId) {
        log.info("Calculating cost split for ride detail ID: {}", rideDetailId);

        // 1. Fetch ride detail
        RideDetail rideDetail = rideDetailRepository.findById(rideDetailId)
                .orElseThrow(() -> {
                    log.warn("Ride detail not found: {}", rideDetailId);
                    return new ValidateRecordException(
                            environment.getProperty(RECORD_NOT_FOUND), "message");
                });

        BigDecimal perKmRate = rideDetail.getPerKmRate();
        if (perKmRate == null || perKmRate.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Per km rate not set on ride detail ID: {}", rideDetailId);
            throw new ValidateRecordException(
                    environment.getProperty(VEHICLE_TYPE_RATE_NOT_CONFIGURED), "message");
        }

        // 2. Fetch all active passengers for this ride
        List<ShareRideDetail> passengers = shareRideDetailRepository
                .findByRideDetailIdAndStatus(rideDetailId, "ACTIVE");

        log.info("Found {} active passengers for ride {}", passengers.size(), rideDetailId);

        // 3. If no passengers, the driver pays everything — return simple response
        if (passengers.isEmpty()) {
            return buildNoPassengerResponse(rideDetail, perKmRate);
        }

        // 4. Build ordered waypoints along the route
        List<Waypoint> waypoints = buildOrderedWaypoints(rideDetail, passengers);

        // 5. Delete old segments and recalculate
        rideSegmentRepository.deleteByRideDetailId(rideDetailId);

        // 6. Build segments between consecutive waypoints
        List<RideSegment> segments = new ArrayList<>();
        List<CostSplitResponse.SegmentDetail> segmentDetails = new ArrayList<>();

        for (int i = 0; i < waypoints.size() - 1; i++) {
            Waypoint start = waypoints.get(i);
            Waypoint end = waypoints.get(i + 1);

            // Calculate distance for this segment
            BigDecimal segmentDistance = calculateSegmentDistance(start, end, rideDetail, waypoints);

            // Determine who is riding on this segment
            // The driver is always on every segment.
            // A passenger is on a segment if the segment falls between their pickup and dropoff.
            int riderCount = 1; // driver always counts
            for (ShareRideDetail p : passengers) {
                if (isPassengerOnSegment(p, start, end, waypoints)) {
                    riderCount++;
                }
            }

            BigDecimal segmentCost = segmentDistance.multiply(perKmRate);
            BigDecimal costPerRider = segmentCost.divide(
                    BigDecimal.valueOf(riderCount), 2, RoundingMode.HALF_UP);

            // Persist segment
            RideSegment segment = new RideSegment();
            segment.setRideDetail(rideDetail);
            segment.setSegmentOrder(i + 1);
            segment.setStartLatitude(start.latitude);
            segment.setStartLongitude(start.longitude);
            segment.setEndLatitude(end.latitude);
            segment.setEndLongitude(end.longitude);
            segment.setStartLabel(start.label);
            segment.setEndLabel(end.label);
            segment.setDistanceKm(segmentDistance);
            segment.setRiderCount(riderCount);
            segment.setSegmentCost(segmentCost);
            segment.setCostPerRider(costPerRider);
            segment.setCreatedDate(DateUtil.getDate());
            segment.setCreatedUser(LoginAuthentication.getUserName());
            segment.setSyncTs(DateUtil.getDate());

            segments.add(segment);

            segmentDetails.add(CostSplitResponse.SegmentDetail.builder()
                    .segmentOrder(i + 1)
                    .startLabel(start.label)
                    .endLabel(end.label)
                    .distanceKm(segmentDistance)
                    .riderCount(riderCount)
                    .segmentCost(segmentCost)
                    .costPerRider(costPerRider)
                    .build());
        }

        rideSegmentRepository.saveAll(segments);

        // 7. Calculate per-passenger costs
        List<CostSplitResponse.PassengerCostDetail> passengerCosts = new ArrayList<>();
        BigDecimal totalPassengerPayments = BigDecimal.ZERO;

        for (ShareRideDetail passenger : passengers) {
            BigDecimal passengerTotal = BigDecimal.ZERO;
            List<CostSplitResponse.PassengerSegmentCost> passengerSegments = new ArrayList<>();

            for (int i = 0; i < waypoints.size() - 1; i++) {
                Waypoint start = waypoints.get(i);
                Waypoint end = waypoints.get(i + 1);

                if (isPassengerOnSegment(passenger, start, end, waypoints)) {
                    RideSegment seg = segments.get(i);
                    BigDecimal share = seg.getCostPerRider();
                    passengerTotal = passengerTotal.add(share);

                    passengerSegments.add(CostSplitResponse.PassengerSegmentCost.builder()
                            .segmentOrder(i + 1)
                            .startLabel(start.label)
                            .endLabel(end.label)
                            .distanceKm(seg.getDistanceKm())
                            .riderCount(seg.getRiderCount())
                            .passengerShareForSegment(share)
                            .build());
                }
            }

            // Update the passenger's cost in the database
            passenger.setPassengerCost(passengerTotal);
            passenger.setModifiedDate(DateUtil.getDate());
            passenger.setModifiedUser(LoginAuthentication.getUserName());
            shareRideDetailRepository.save(passenger);

            totalPassengerPayments = totalPassengerPayments.add(passengerTotal);

            passengerCosts.add(CostSplitResponse.PassengerCostDetail.builder()
                    .userId(passenger.getUser().getId())
                    .shareRideDetailId(passenger.getId())
                    .startCity(passenger.getStartCity())
                    .endCity(passenger.getEndCity())
                    .passengerRideDistance(passenger.getPassengerRideDistance())
                    .totalPassengerCost(passengerTotal)
                    .segmentBreakdown(passengerSegments)
                    .build());
        }

        // 8. Driver effective cost = total - all passenger payments
        BigDecimal totalRideCost = rideDetail.getTotalRideDistance().multiply(perKmRate);
        BigDecimal driverEffectiveCost = totalRideCost.subtract(totalPassengerPayments);

        log.info("Cost split calculated: totalCost={}, driverPays={}, passengers={}",
                totalRideCost, driverEffectiveCost, passengers.size());

        return CostSplitResponse.builder()
                .rideDetailId(rideDetailId)
                .totalRideCost(totalRideCost)
                .totalRideDistance(rideDetail.getTotalRideDistance())
                .perKmRate(perKmRate)
                .driverEffectiveCost(driverEffectiveCost)
                .driverStartCity(rideDetail.getStartCity())
                .totalPassengers(passengers.size())
                .segments(segmentDetails)
                .passengerCosts(passengerCosts)
                .build();
    }

    @Override
    public CostSplitResponse getCostSplit(Long rideDetailId) {
        // Check if segments already exist
        List<RideSegment> existingSegments = rideSegmentRepository
                .findByRideDetailIdOrderBySegmentOrder(rideDetailId);

        // If no segments exist, calculate fresh
        if (existingSegments.isEmpty()) {
            return calculateCostSplit(rideDetailId);
        }

        // Rebuild response from persisted data
        RideDetail rideDetail = rideDetailRepository.findById(rideDetailId)
                .orElseThrow(() -> new ValidateRecordException(
                        environment.getProperty(RECORD_NOT_FOUND), "message"));

        List<ShareRideDetail> passengers = shareRideDetailRepository
                .findByRideDetailIdAndStatus(rideDetailId, "ACTIVE");

        BigDecimal perKmRate = rideDetail.getPerKmRate();
        BigDecimal totalRideCost = rideDetail.getTotalRideDistance().multiply(perKmRate);

        // Build segment details from persisted data
        List<CostSplitResponse.SegmentDetail> segmentDetails = existingSegments.stream()
                .map(seg -> CostSplitResponse.SegmentDetail.builder()
                        .segmentOrder(seg.getSegmentOrder())
                        .startLabel(seg.getStartLabel())
                        .endLabel(seg.getEndLabel())
                        .distanceKm(seg.getDistanceKm())
                        .riderCount(seg.getRiderCount())
                        .segmentCost(seg.getSegmentCost())
                        .costPerRider(seg.getCostPerRider())
                        .build())
                .collect(Collectors.toList());

        // Build passenger cost details from persisted data
        BigDecimal totalPassengerPayments = BigDecimal.ZERO;
        List<CostSplitResponse.PassengerCostDetail> passengerCosts = new ArrayList<>();

        for (ShareRideDetail passenger : passengers) {
            // Build segment breakdown for this passenger from persisted segments
            List<CostSplitResponse.PassengerSegmentCost> segBreakdown = new ArrayList<>();
            BigDecimal passengerTotal = passenger.getPassengerCost();

            for (RideSegment seg : existingSegments) {
                // Re-check if this passenger was on this segment
                if (isPassengerOnPersistedSegment(passenger, seg)) {
                    segBreakdown.add(CostSplitResponse.PassengerSegmentCost.builder()
                            .segmentOrder(seg.getSegmentOrder())
                            .startLabel(seg.getStartLabel())
                            .endLabel(seg.getEndLabel())
                            .distanceKm(seg.getDistanceKm())
                            .riderCount(seg.getRiderCount())
                            .passengerShareForSegment(seg.getCostPerRider())
                            .build());
                }
            }

            totalPassengerPayments = totalPassengerPayments.add(passengerTotal);

            passengerCosts.add(CostSplitResponse.PassengerCostDetail.builder()
                    .userId(passenger.getUser().getId())
                    .shareRideDetailId(passenger.getId())
                    .startCity(passenger.getStartCity())
                    .endCity(passenger.getEndCity())
                    .passengerRideDistance(passenger.getPassengerRideDistance())
                    .totalPassengerCost(passengerTotal)
                    .segmentBreakdown(segBreakdown)
                    .build());
        }

        BigDecimal driverEffectiveCost = totalRideCost.subtract(totalPassengerPayments);

        return CostSplitResponse.builder()
                .rideDetailId(rideDetailId)
                .totalRideCost(totalRideCost)
                .totalRideDistance(rideDetail.getTotalRideDistance())
                .perKmRate(perKmRate)
                .driverEffectiveCost(driverEffectiveCost)
                .driverStartCity(rideDetail.getStartCity())
                .totalPassengers(passengers.size())
                .segments(segmentDetails)
                .passengerCosts(passengerCosts)
                .build();
    }

    // ─── Private Helper Methods ─────────────────────────────────────────

    /**
     * Simple response when there are no passengers — driver pays full cost.
     */
    private CostSplitResponse buildNoPassengerResponse(RideDetail rideDetail, BigDecimal perKmRate) {
        BigDecimal totalCost = rideDetail.getTotalRideDistance().multiply(perKmRate);
        return CostSplitResponse.builder()
                .rideDetailId(rideDetail.getId())
                .totalRideCost(totalCost)
                .totalRideDistance(rideDetail.getTotalRideDistance())
                .perKmRate(perKmRate)
                .driverEffectiveCost(totalCost)
                .driverStartCity(rideDetail.getStartCity())
                .totalPassengers(0)
                .segments(Collections.emptyList())
                .passengerCosts(Collections.emptyList())
                .build();
    }

    /**
     * Build an ordered list of waypoints along the driver's route.
     * Waypoints include: driver start, all passenger pickups, all passenger dropoffs, driver end.
     * They are ordered by their projected distance along the route.
     */
    private List<Waypoint> buildOrderedWaypoints(RideDetail rideDetail, List<ShareRideDetail> passengers) {
        List<Waypoint> waypoints = new ArrayList<>();

        // Driver start
        waypoints.add(new Waypoint(
                rideDetail.getStartLocationLatitude(),
                rideDetail.getStartLocationLongitude(),
                rideDetail.getStartCity() != null ? rideDetail.getStartCity() : "Start",
                WaypointType.DRIVER_START,
                null
        ));

        // Driver end
        waypoints.add(new Waypoint(
                rideDetail.getEndLocationLatitude(),
                rideDetail.getEndLocationLongitude(),
                "Destination",
                WaypointType.DRIVER_END,
                null
        ));

        // Passenger pickups and dropoffs
        for (ShareRideDetail p : passengers) {
            waypoints.add(new Waypoint(
                    p.getStartLocationLatitude(),
                    p.getStartLocationLongitude(),
                    p.getStartCity() != null ? p.getStartCity() : "Pickup",
                    WaypointType.PASSENGER_PICKUP,
                    p.getId()
            ));
            waypoints.add(new Waypoint(
                    p.getEndLocationLatitude(),
                    p.getEndLocationLongitude(),
                    p.getEndCity() != null ? p.getEndCity() : "Dropoff",
                    WaypointType.PASSENGER_DROPOFF,
                    p.getId()
            ));
        }

        // Order waypoints by their distance from the driver's start point along the route.
        // We project each waypoint onto the driver's route using straight-line distance
        // from the start as a proxy for route position.
        BigDecimal driverStartLat = rideDetail.getStartLocationLatitude();
        BigDecimal driverStartLng = rideDetail.getStartLocationLongitude();

        waypoints.sort((a, b) -> {
            double distA = haversineDistance(
                    driverStartLat.doubleValue(), driverStartLng.doubleValue(),
                    a.latitude.doubleValue(), a.longitude.doubleValue());
            double distB = haversineDistance(
                    driverStartLat.doubleValue(), driverStartLng.doubleValue(),
                    b.latitude.doubleValue(), b.longitude.doubleValue());
            return Double.compare(distA, distB);
        });

        // Remove duplicate waypoints (same lat/lng within tolerance)
        waypoints = deduplicateWaypoints(waypoints);

        return waypoints;
    }

    /**
     * Calculate the distance of a segment.
     * Uses proportional allocation of the total ride distance based on
     * the fraction of route distance this segment represents.
     */
    private BigDecimal calculateSegmentDistance(Waypoint start, Waypoint end,
                                                RideDetail rideDetail,
                                                List<Waypoint> allWaypoints) {
        // Calculate total straight-line distance across all waypoints
        double totalStraightLine = 0;
        for (int i = 0; i < allWaypoints.size() - 1; i++) {
            totalStraightLine += haversineDistance(
                    allWaypoints.get(i).latitude.doubleValue(),
                    allWaypoints.get(i).longitude.doubleValue(),
                    allWaypoints.get(i + 1).latitude.doubleValue(),
                    allWaypoints.get(i + 1).longitude.doubleValue());
        }

        if (totalStraightLine <= 0) {
            // Fallback: divide total distance equally among segments
            return rideDetail.getTotalRideDistance().divide(
                    BigDecimal.valueOf(allWaypoints.size() - 1), 2, RoundingMode.HALF_UP);
        }

        // This segment's straight-line distance
        double segmentStraightLine = haversineDistance(
                start.latitude.doubleValue(), start.longitude.doubleValue(),
                end.latitude.doubleValue(), end.longitude.doubleValue());

        // Proportional share of actual road distance
        double fraction = segmentStraightLine / totalStraightLine;
        return rideDetail.getTotalRideDistance()
                .multiply(BigDecimal.valueOf(fraction))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Check if a passenger is riding on a given segment.
     * A passenger is on a segment if:
     *   - The segment starts at or after the passenger's pickup waypoint index
     *   - The segment ends at or before the passenger's dropoff waypoint index
     */
    private boolean isPassengerOnSegment(ShareRideDetail passenger, Waypoint segStart,
                                          Waypoint segEnd, List<Waypoint> waypoints) {
        int pickupIndex = -1;
        int dropoffIndex = -1;
        int segStartIndex = -1;
        int segEndIndex = -1;

        for (int i = 0; i < waypoints.size(); i++) {
            Waypoint wp = waypoints.get(i);

            if (wp.type == WaypointType.PASSENGER_PICKUP
                    && Objects.equals(wp.shareRideDetailId, passenger.getId())) {
                pickupIndex = i;
            }
            if (wp.type == WaypointType.PASSENGER_DROPOFF
                    && Objects.equals(wp.shareRideDetailId, passenger.getId())) {
                dropoffIndex = i;
            }
            if (wp == segStart) segStartIndex = i;
            if (wp == segEnd) segEndIndex = i;
        }

        if (pickupIndex == -1 || dropoffIndex == -1 || segStartIndex == -1 || segEndIndex == -1) {
            return false;
        }

        // Passenger is on this segment if:
        // segment starts at or after pickup AND segment ends at or before dropoff
        return segStartIndex >= pickupIndex && segEndIndex <= dropoffIndex;
    }

    /**
     * Check if a passenger was on a persisted segment (for reading cached results).
     * Uses coordinate comparison.
     */
    private boolean isPassengerOnPersistedSegment(ShareRideDetail passenger, RideSegment segment) {
        // Check if segment midpoint falls within passenger's ride range
        double segMidLat = (segment.getStartLatitude().doubleValue() + segment.getEndLatitude().doubleValue()) / 2;
        double segMidLng = (segment.getStartLongitude().doubleValue() + segment.getEndLongitude().doubleValue()) / 2;

        double pickupToSegMid = haversineDistance(
                passenger.getStartLocationLatitude().doubleValue(),
                passenger.getStartLocationLongitude().doubleValue(),
                segMidLat, segMidLng);

        double dropoffToSegMid = haversineDistance(
                passenger.getEndLocationLatitude().doubleValue(),
                passenger.getEndLocationLongitude().doubleValue(),
                segMidLat, segMidLng);

        double passengerRideDist = haversineDistance(
                passenger.getStartLocationLatitude().doubleValue(),
                passenger.getStartLocationLongitude().doubleValue(),
                passenger.getEndLocationLatitude().doubleValue(),
                passenger.getEndLocationLongitude().doubleValue());

        // If the sum of distances from pickup and dropoff to segment midpoint
        // is approximately equal to the passenger's total ride distance,
        // the segment midpoint lies between pickup and dropoff
        return (pickupToSegMid + dropoffToSegMid) <= (passengerRideDist * 1.1 + 0.5);
    }

    /**
     * Remove waypoints that are too close together (within 100m).
     * Keeps the first occurrence, merging labels.
     */
    private List<Waypoint> deduplicateWaypoints(List<Waypoint> waypoints) {
        if (waypoints.size() <= 2) return waypoints;

        List<Waypoint> result = new ArrayList<>();
        result.add(waypoints.get(0));

        for (int i = 1; i < waypoints.size(); i++) {
            Waypoint prev = result.get(result.size() - 1);
            Waypoint curr = waypoints.get(i);

            double dist = haversineDistance(
                    prev.latitude.doubleValue(), prev.longitude.doubleValue(),
                    curr.latitude.doubleValue(), curr.longitude.doubleValue());

            if (dist < 0.1) { // Less than 100 meters apart — merge
                // Keep the one with the more meaningful type
                if (curr.type == WaypointType.DRIVER_START || curr.type == WaypointType.DRIVER_END) {
                    // Driver waypoints take precedence — replace
                    result.set(result.size() - 1, curr);
                }
                // Otherwise keep prev (already in list)
            } else {
                result.add(curr);
            }
        }

        return result;
    }

    /**
     * Haversine formula to calculate the great-circle distance between two points (in km).
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Earth's radius in kilometers

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    // ─── Internal Data Structures ───────────────────────────────────────

    private enum WaypointType {
        DRIVER_START,
        DRIVER_END,
        PASSENGER_PICKUP,
        PASSENGER_DROPOFF
    }

    private static class Waypoint {
        final BigDecimal latitude;
        final BigDecimal longitude;
        final String label;
        final WaypointType type;
        final Long shareRideDetailId; // null for driver waypoints

        Waypoint(BigDecimal latitude, BigDecimal longitude, String label,
                 WaypointType type, Long shareRideDetailId) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.label = label;
            this.type = type;
            this.shareRideDetailId = shareRideDetailId;
        }
    }
}

