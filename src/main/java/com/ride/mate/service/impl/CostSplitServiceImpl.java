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
 * Daily commuter ride-share cost algorithm (from costsplittinglogic.md):
 *
 * The driver is already travelling this route for their own purpose — the route is NOT
 * created for passengers.  Passengers pay a fair share of running costs, not a full
 * private-hire fee.
 *
 * MAIN segments (driver's regular commute road):
 *   N = number of passengers currently in the car
 *   Share % per passenger = max(60 / N, 20)
 *     N=1 → 60%,  N=2 → 40%,  N=3 → 30%,  N=4 → 25%,  N≥5 → 20% (floor)
 *   Driver alone (N=0) → no charge to anyone
 *
 * SIDE_TRIP segments (detour off main road to pick up / drop off one passenger):
 *   That passenger pays 60% of the detour cost.
 *   Driver absorbs 40% (goodwill).
 *   No other passenger is charged for the detour.
 *
 * Example (cost = Rs.1/km):
 *   Driver alone (5 km)                    → Rs.0   for passengers
 *   P1 side-trip pickup (10 km)             → P1 = Rs.6
 *   Driver + P1 (10 km)                     → P1 = Rs.6        (60%)
 *   P2 side-trip pickup (16 km)             → P2 = Rs.9.60
 *   Driver + P1 + P2 (15 km)               → P1 = Rs.6, P2 = Rs.6  (40% each)
 *   Driver + P1 + P2 + P3 (40 km)          → each Rs.12.00         (30% each)
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Tishan           Initial Development
 * 2 21-03-2026    N/A          N/A          Tishan           Replaced equal-split with max(60/N,20)% algorithm
 */
@Slf4j
@Service
@Transactional
public class CostSplitServiceImpl extends MessagePropertyBase implements CostSplitService {

    /** Segment type constant: driver's regular commute road */
    private static final String SEGMENT_TYPE_MAIN = "MAIN";

    /** Segment type constant: detour off main road for one passenger */
    private static final String SEGMENT_TYPE_SIDE_TRIP = "SIDE_TRIP";

    /** Share % a single passenger pays on a SIDE_TRIP segment */
    private static final BigDecimal SIDE_TRIP_PASSENGER_SHARE_PCT = new BigDecimal("60");

    /** Minimum share % any passenger ever pays on a MAIN segment */
    private static final BigDecimal MIN_SHARE_PCT = new BigDecimal("20");

    /** Base share % for a single passenger on a MAIN segment */
    private static final BigDecimal BASE_SHARE_PCT = new BigDecimal("60");

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

    // ═══════════════════════════════════════════════════════════════════
    //  Public API
    // ═══════════════════════════════════════════════════════════════════

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

        // 3. No passengers → driver pays everything
        if (passengers.isEmpty()) {
            return buildNoPassengerResponse(rideDetail, perKmRate);
        }

        // 4. Build ordered waypoints along the route
        List<Waypoint> waypoints = buildOrderedWaypoints(rideDetail, passengers);

        // 5. Delete previously persisted segments so we start fresh
        rideSegmentRepository.deleteByRideDetailId(rideDetailId);

        // 6. Build segments between consecutive waypoints
        List<RideSegment> persistedSegments = new ArrayList<>();
        List<CostSplitResponse.SegmentDetail> segmentDetails = new ArrayList<>();

        // Per-passenger running cost accumulators
        Map<Long, BigDecimal> passengerTotals = new LinkedHashMap<>();
        Map<Long, List<CostSplitResponse.PassengerSegmentCost>> passengerSegmentBreakdowns = new LinkedHashMap<>();
        for (ShareRideDetail p : passengers) {
            passengerTotals.put(p.getId(), BigDecimal.ZERO);
            passengerSegmentBreakdowns.put(p.getId(), new ArrayList<>());
        }

        for (int i = 0; i < waypoints.size() - 1; i++) {
            Waypoint segStart = waypoints.get(i);
            Waypoint segEnd   = waypoints.get(i + 1);

            BigDecimal segmentDistance = calculateSegmentDistance(segStart, segEnd, rideDetail, waypoints);

            // Skip zero-distance segments
            if (segmentDistance.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            BigDecimal segmentCost = segmentDistance.multiply(perKmRate).setScale(2, RoundingMode.HALF_UP);

            // ── Determine segment type ──────────────────────────────────
            // A SIDE_TRIP segment is the round-trip detour to pick up or drop off exactly
            // one passenger whose pickup/dropoff waypoint is the END of this segment and
            // the waypoint is NOT on the driver's direct line between start and final destination.
            // In this implementation we mark a segment as SIDE_TRIP when its end waypoint is
            // a passenger pickup or dropoff that is the ONLY passenger boundary at that point
            // AND the next waypoint backtracks (returns to where we came from).
            //
            // Practical approach: the front-end / ML service supplies a detour distance in
            // the ShareRideDetail.  If a passenger has a non-zero detour distance stored,
            // their pickup & dropoff are treated as SIDE_TRIP segments; otherwise MAIN.
            // For now we classify as SIDE_TRIP only when the end waypoint is a solo
            // passenger boundary (pickup or dropoff) AND the waypoint after it goes back
            // in the direction of the previous waypoint (i.e., it's an out-and-back detour).

            String segmentType = classifySegmentType(segStart, segEnd, waypoints, i);
            Long sideTripPassengerId = (SEGMENT_TYPE_SIDE_TRIP.equals(segmentType))
                    ? getSideTripPassengerId(segEnd)
                    : null;

            // ── Passengers present on this segment ──────────────────────
            // For MAIN: all passengers whose ride range includes this segment.
            // For SIDE_TRIP: only the one passenger this detour is for.
            List<ShareRideDetail> passengersOnSegment;
            if (SEGMENT_TYPE_SIDE_TRIP.equals(segmentType) && sideTripPassengerId != null) {
                final Long stpId = sideTripPassengerId;
                passengersOnSegment = passengers.stream()
                        .filter(p -> p.getId().equals(stpId))
                        .collect(Collectors.toList());
            } else {
                passengersOnSegment = passengers.stream()
                        .filter(p -> isPassengerOnSegment(p, segStart, segEnd, waypoints))
                        .collect(Collectors.toList());
            }

            int passengerCount = passengersOnSegment.size();

            // ── Share percentage ─────────────────────────────────────────
            BigDecimal sharePct;
            BigDecimal costPerRider;

            if (SEGMENT_TYPE_SIDE_TRIP.equals(segmentType)) {
                // Detour: that one passenger pays 60%, driver absorbs 40%
                sharePct    = SIDE_TRIP_PASSENGER_SHARE_PCT;
                costPerRider = segmentCost
                        .multiply(sharePct)
                        .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            } else {
                if (passengerCount == 0) {
                    // Driver alone — no passenger charge
                    sharePct     = BigDecimal.ZERO;
                    costPerRider = BigDecimal.ZERO;
                } else {
                    // max(60 / N, 20) %
                    sharePct = BASE_SHARE_PCT
                            .divide(BigDecimal.valueOf(passengerCount), 4, RoundingMode.HALF_UP)
                            .max(MIN_SHARE_PCT)
                            .setScale(2, RoundingMode.HALF_UP);
                    costPerRider = segmentCost
                            .multiply(sharePct)
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
                }
            }

            int riderCount = passengerCount;

            // ── Persist segment ─────────────────────────────────────────
            RideSegment segment = new RideSegment();
            segment.setRideDetail(rideDetail);
            segment.setSegmentOrder(i + 1);
            segment.setStartLatitude(segStart.latitude);
            segment.setStartLongitude(segStart.longitude);
            segment.setEndLatitude(segEnd.latitude);
            segment.setEndLongitude(segEnd.longitude);
            segment.setStartLabel(segStart.label);
            segment.setEndLabel(segEnd.label);
            segment.setDistanceKm(segmentDistance);
            segment.setRiderCount(riderCount);
            segment.setSegmentCost(segmentCost);
            segment.setCostPerRider(costPerRider);
            segment.setSegmentType(segmentType);
            segment.setSharePercentage(sharePct);
            segment.setCreatedDate(DateUtil.getDate());
            segment.setCreatedUser(LoginAuthentication.getUserName());
            segment.setSyncTs(DateUtil.getDate());
            persistedSegments.add(segment);

            segmentDetails.add(CostSplitResponse.SegmentDetail.builder()
                    .segmentOrder(i + 1)
                    .startLabel(segStart.label)
                    .endLabel(segEnd.label)
                    .distanceKm(segmentDistance)
                    .riderCount(riderCount)
                    .segmentType(segmentType)
                    .sharePercentage(sharePct)
                    .segmentCost(segmentCost)
                    .costPerRider(costPerRider)
                    .build());

            // ── Accumulate per-passenger costs ───────────────────────────
            for (ShareRideDetail p : passengersOnSegment) {
                passengerTotals.merge(p.getId(), costPerRider, BigDecimal::add);
                passengerSegmentBreakdowns.get(p.getId()).add(
                        CostSplitResponse.PassengerSegmentCost.builder()
                                .segmentOrder(i + 1)
                                .startLabel(segStart.label)
                                .endLabel(segEnd.label)
                                .distanceKm(segmentDistance)
                                .riderCount(riderCount)
                                .segmentType(segmentType)
                                .sharePercentage(sharePct)
                                .passengerShareForSegment(costPerRider)
                                .build());
            }
        }

        rideSegmentRepository.saveAll(persistedSegments);

        // 7. Build passenger cost details and persist updated costs
        List<CostSplitResponse.PassengerCostDetail> passengerCosts = new ArrayList<>();
        BigDecimal totalPassengerPayments = BigDecimal.ZERO;

        for (ShareRideDetail passenger : passengers) {
            BigDecimal passengerTotal = passengerTotals.getOrDefault(passenger.getId(), BigDecimal.ZERO);

            // Persist updated cost back to shared_ride_detail
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
                    .segmentBreakdown(passengerSegmentBreakdowns.getOrDefault(
                            passenger.getId(), Collections.emptyList()))
                    .build());
        }

        // 8. Driver effective cost = total ride cost − total passenger payments
        BigDecimal totalRideCost = rideDetail.getTotalRideDistance()
                .multiply(perKmRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal driverEffectiveCost = totalRideCost.subtract(totalPassengerPayments);

        log.info("Cost split calculated — totalCost={}, driverPays={}, passengers={}",
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
        // If no segments persisted yet, calculate fresh
        List<RideSegment> existingSegments = rideSegmentRepository
                .findByRideDetailIdOrderBySegmentOrder(rideDetailId);

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
        BigDecimal totalRideCost = rideDetail.getTotalRideDistance()
                .multiply(perKmRate).setScale(2, RoundingMode.HALF_UP);

        // Build segment details from persisted data (includes segmentType + sharePercentage)
        List<CostSplitResponse.SegmentDetail> segmentDetails = existingSegments.stream()
                .map(seg -> CostSplitResponse.SegmentDetail.builder()
                        .segmentOrder(seg.getSegmentOrder())
                        .startLabel(seg.getStartLabel())
                        .endLabel(seg.getEndLabel())
                        .distanceKm(seg.getDistanceKm())
                        .riderCount(seg.getRiderCount())
                        .segmentType(seg.getSegmentType())
                        .sharePercentage(seg.getSharePercentage())
                        .segmentCost(seg.getSegmentCost())
                        .costPerRider(seg.getCostPerRider())
                        .build())
                .collect(Collectors.toList());

        // Build passenger cost details from persisted passenger records
        BigDecimal totalPassengerPayments = BigDecimal.ZERO;
        List<CostSplitResponse.PassengerCostDetail> passengerCosts = new ArrayList<>();

        for (ShareRideDetail passenger : passengers) {
            BigDecimal passengerTotal = passenger.getPassengerCost() != null
                    ? passenger.getPassengerCost() : BigDecimal.ZERO;

            List<CostSplitResponse.PassengerSegmentCost> segBreakdown = existingSegments.stream()
                    .filter(seg -> isPassengerOnPersistedSegment(passenger, seg))
                    .map(seg -> CostSplitResponse.PassengerSegmentCost.builder()
                            .segmentOrder(seg.getSegmentOrder())
                            .startLabel(seg.getStartLabel())
                            .endLabel(seg.getEndLabel())
                            .distanceKm(seg.getDistanceKm())
                            .riderCount(seg.getRiderCount())
                            .segmentType(seg.getSegmentType())
                            .sharePercentage(seg.getSharePercentage())
                            .passengerShareForSegment(seg.getCostPerRider())
                            .build())
                    .collect(Collectors.toList());

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

    // ═══════════════════════════════════════════════════════════════════
    //  Algorithm Helpers
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Classify a segment as MAIN or SIDE_TRIP.
     *
     * A segment is a SIDE_TRIP when its END waypoint is a lone passenger boundary
     * (pickup or dropoff) AND the very next waypoint in the list reverses direction —
     * i.e., it is closer to the CURRENT segment's START than to the segment's END.
     * This identifies classic out-and-back detours.
     */
    private String classifySegmentType(Waypoint segStart, Waypoint segEnd,
                                        List<Waypoint> waypoints, int segEndIndex) {
        // End waypoint must be a passenger pickup or dropoff
        if (segEnd.type != WaypointType.PASSENGER_PICKUP
                && segEnd.type != WaypointType.PASSENGER_DROPOFF) {
            return SEGMENT_TYPE_MAIN;
        }

        // There must be a waypoint after the end to check for backtracking
        int nextIndex = segEndIndex + 2; // segEndIndex is the index of segStart; segEnd is +1
        if (nextIndex >= waypoints.size()) {
            return SEGMENT_TYPE_MAIN;
        }

        Waypoint nextWaypoint = waypoints.get(nextIndex);

        // If the next waypoint is closer to segStart than to segEnd, it's a backtrack → SIDE_TRIP
        double distToStart = haversineDistance(
                segEnd.latitude.doubleValue(), segEnd.longitude.doubleValue(),
                segStart.latitude.doubleValue(), segStart.longitude.doubleValue());
        double distToNext = haversineDistance(
                segEnd.latitude.doubleValue(), segEnd.longitude.doubleValue(),
                nextWaypoint.latitude.doubleValue(), nextWaypoint.longitude.doubleValue());

        // If the next waypoint is very close to segStart (within 10% of the detour distance),
        // this is an out-and-back detour
        return (distToNext < distToStart * 1.1) ? SEGMENT_TYPE_SIDE_TRIP : SEGMENT_TYPE_MAIN;
    }

    /**
     * Return the shareRideDetailId of the passenger this SIDE_TRIP segment belongs to.
     * The benefiting passenger is identified by the end waypoint of the detour.
     */
    private Long getSideTripPassengerId(Waypoint endWaypoint) {
        return endWaypoint.shareRideDetailId;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Waypoint Helpers
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Response when there are no passengers — driver bears full cost.
     */
    private CostSplitResponse buildNoPassengerResponse(RideDetail rideDetail, BigDecimal perKmRate) {
        BigDecimal totalCost = rideDetail.getTotalRideDistance()
                .multiply(perKmRate).setScale(2, RoundingMode.HALF_UP);
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
     * Order: driver start → passenger pickups/dropoffs sorted by distance from start → driver end.
     */
    private List<Waypoint> buildOrderedWaypoints(RideDetail rideDetail, List<ShareRideDetail> passengers) {
        List<Waypoint> waypoints = new ArrayList<>();

        waypoints.add(new Waypoint(
                rideDetail.getStartLocationLatitude(),
                rideDetail.getStartLocationLongitude(),
                rideDetail.getStartCity() != null ? rideDetail.getStartCity() : "Start",
                WaypointType.DRIVER_START, null));

        for (ShareRideDetail p : passengers) {
            waypoints.add(new Waypoint(
                    p.getStartLocationLatitude(),
                    p.getStartLocationLongitude(),
                    p.getStartCity() != null ? p.getStartCity() : "Pickup",
                    WaypointType.PASSENGER_PICKUP, p.getId()));

            waypoints.add(new Waypoint(
                    p.getEndLocationLatitude(),
                    p.getEndLocationLongitude(),
                    p.getEndCity() != null ? p.getEndCity() : "Dropoff",
                    WaypointType.PASSENGER_DROPOFF, p.getId()));
        }

        waypoints.add(new Waypoint(
                rideDetail.getEndLocationLatitude(),
                rideDetail.getEndLocationLongitude(),
                rideDetail.getEndCity() != null ? rideDetail.getEndCity() : "Destination",
                WaypointType.DRIVER_END, null));

        // Sort by straight-line distance from driver's start (proxy for route order)
        final BigDecimal startLat = rideDetail.getStartLocationLatitude();
        final BigDecimal startLng = rideDetail.getStartLocationLongitude();

        waypoints.sort(Comparator.comparingDouble(wp ->
                haversineDistance(startLat.doubleValue(), startLng.doubleValue(),
                        wp.latitude.doubleValue(), wp.longitude.doubleValue())));

        // Ensure driver start is always first and driver end always last
        waypoints.removeIf(wp -> wp.type == WaypointType.DRIVER_START || wp.type == WaypointType.DRIVER_END);
        waypoints.add(0, new Waypoint(
                rideDetail.getStartLocationLatitude(),
                rideDetail.getStartLocationLongitude(),
                rideDetail.getStartCity() != null ? rideDetail.getStartCity() : "Start",
                WaypointType.DRIVER_START, null));
        waypoints.add(new Waypoint(
                rideDetail.getEndLocationLatitude(),
                rideDetail.getEndLocationLongitude(),
                rideDetail.getEndCity() != null ? rideDetail.getEndCity() : "Destination",
                WaypointType.DRIVER_END, null));

        return deduplicateWaypoints(waypoints);
    }

    /**
     * Proportionally allocate the total ride distance to a segment using straight-line ratios.
     */
    private BigDecimal calculateSegmentDistance(Waypoint start, Waypoint end,
                                                 RideDetail rideDetail,
                                                 List<Waypoint> allWaypoints) {
        double totalStraightLine = 0;
        for (int i = 0; i < allWaypoints.size() - 1; i++) {
            totalStraightLine += haversineDistance(
                    allWaypoints.get(i).latitude.doubleValue(),
                    allWaypoints.get(i).longitude.doubleValue(),
                    allWaypoints.get(i + 1).latitude.doubleValue(),
                    allWaypoints.get(i + 1).longitude.doubleValue());
        }

        if (totalStraightLine <= 0) {
            return rideDetail.getTotalRideDistance()
                    .divide(BigDecimal.valueOf(allWaypoints.size() - 1), 2, RoundingMode.HALF_UP);
        }

        double segStraightLine = haversineDistance(
                start.latitude.doubleValue(), start.longitude.doubleValue(),
                end.latitude.doubleValue(), end.longitude.doubleValue());

        double fraction = segStraightLine / totalStraightLine;
        return rideDetail.getTotalRideDistance()
                .multiply(BigDecimal.valueOf(fraction))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * A passenger is present on a MAIN segment when the segment lies entirely within
     * [their pickup waypoint index, their dropoff waypoint index].
     */
    private boolean isPassengerOnSegment(ShareRideDetail passenger,
                                          Waypoint segStart, Waypoint segEnd,
                                          List<Waypoint> waypoints) {
        int pickupIdx  = -1;
        int dropoffIdx = -1;
        int startIdx   = -1;
        int endIdx     = -1;

        for (int i = 0; i < waypoints.size(); i++) {
            Waypoint wp = waypoints.get(i);
            if (wp.type == WaypointType.PASSENGER_PICKUP
                    && Objects.equals(wp.shareRideDetailId, passenger.getId())) pickupIdx = i;
            if (wp.type == WaypointType.PASSENGER_DROPOFF
                    && Objects.equals(wp.shareRideDetailId, passenger.getId())) dropoffIdx = i;
            if (wp == segStart) startIdx = i;
            if (wp == segEnd)   endIdx   = i;
        }

        if (pickupIdx == -1 || dropoffIdx == -1 || startIdx == -1 || endIdx == -1) {
            return false;
        }
        return startIdx >= pickupIdx && endIdx <= dropoffIdx;
    }

    /**
     * For the read-back path: determine whether a passenger rode a persisted segment
     * by checking whether the segment midpoint falls within the passenger's coordinate range.
     */
    private boolean isPassengerOnPersistedSegment(ShareRideDetail passenger, RideSegment segment) {
        // SIDE_TRIP segments: check via coordinate proximity to passenger start/end
        double segMidLat = (segment.getStartLatitude().doubleValue()
                + segment.getEndLatitude().doubleValue()) / 2.0;
        double segMidLng = (segment.getStartLongitude().doubleValue()
                + segment.getEndLongitude().doubleValue()) / 2.0;

        double pickupToMid = haversineDistance(
                passenger.getStartLocationLatitude().doubleValue(),
                passenger.getStartLocationLongitude().doubleValue(),
                segMidLat, segMidLng);
        double dropoffToMid = haversineDistance(
                passenger.getEndLocationLatitude().doubleValue(),
                passenger.getEndLocationLongitude().doubleValue(),
                segMidLat, segMidLng);
        double passengerDist = haversineDistance(
                passenger.getStartLocationLatitude().doubleValue(),
                passenger.getStartLocationLongitude().doubleValue(),
                passenger.getEndLocationLatitude().doubleValue(),
                passenger.getEndLocationLongitude().doubleValue());

        // Midpoint is "between" pickup and dropoff if triangle inequality holds within 10% tolerance
        return (pickupToMid + dropoffToMid) <= (passengerDist * 1.1 + 0.5);
    }

    /**
     * Remove waypoints closer than 100 m to their predecessor.
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

            if (dist < 0.1) {
                // Prefer driver waypoints if merging
                if (curr.type == WaypointType.DRIVER_START || curr.type == WaypointType.DRIVER_END) {
                    result.set(result.size() - 1, curr);
                }
            } else {
                result.add(curr);
            }
        }
        return result;
    }

    /**
     * Haversine great-circle distance in kilometres.
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Internal Data Structures
    // ═══════════════════════════════════════════════════════════════════

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
        /** null for driver waypoints */
        final Long shareRideDetailId;

        Waypoint(BigDecimal latitude, BigDecimal longitude, String label,
                 WaypointType type, Long shareRideDetailId) {
            this.latitude        = latitude;
            this.longitude       = longitude;
            this.label           = label;
            this.type            = type;
            this.shareRideDetailId = shareRideDetailId;
        }
    }
}

