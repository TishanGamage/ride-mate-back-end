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

        // 5. Delete previously persisted segments so we start fresh, then flush so
        //    the deleteByRideDetailId is visible within this same transaction before saveAll.
        rideSegmentRepository.deleteByRideDetailId(rideDetailId);
        rideSegmentRepository.flush();

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

        int segmentOrder = 0; // incremented only for non-zero-distance segments

        for (int i = 0; i < waypoints.size() - 1; i++) {
            Waypoint segStart = waypoints.get(i);
            Waypoint segEnd   = waypoints.get(i + 1);

            BigDecimal segmentDistance = calculateSegmentDistance(segStart, segEnd, rideDetail, waypoints);

            // Skip zero-distance segments (e.g. two passengers at the same location)
            if (segmentDistance.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            segmentOrder++;
            BigDecimal segmentCost = segmentDistance.multiply(perKmRate).setScale(2, RoundingMode.HALF_UP);

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
            segment.setSegmentOrder(segmentOrder);
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
                    .segmentOrder(segmentOrder)
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
                                .segmentOrder(segmentOrder)
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

        // If the current passenger count differs from the max riderCount in persisted
        // segments, the segments are stale (e.g. a 2nd passenger was added after the
        // segments were last saved). Recalculate from scratch so the data is always fresh.
        int maxPersistedRiderCount = existingSegments.stream()
                .mapToInt(RideSegment::getRiderCount)
                .max()
                .orElse(0);
        if (passengers.size() != maxPersistedRiderCount) {
            log.info("Stale segments detected for ride {} (persisted maxRiders={}, activePassengers={}). Recalculating.",
                    rideDetailId, maxPersistedRiderCount, passengers.size());
            return calculateCostSplit(rideDetailId);
        }

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
     * A segment is a SIDE_TRIP ONLY when ALL of the following are true:
     *  1. The end waypoint is a passenger pickup or dropoff.
     *  2. EXACTLY ONE distinct passenger has a waypoint at that endpoint
     *     (if two passengers share the same coordinates, it is a shared MAIN segment).
     *  3. The very next waypoint in the list backtracks to within 200 m of segStart —
     *     a true out-and-back detour.
     *
     * Any other case is MAIN.
     */
    private String classifySegmentType(Waypoint segStart, Waypoint segEnd,
                                        List<Waypoint> waypoints, int segEndIndex) {
        // End waypoint must be a passenger pickup or dropoff
        if (segEnd.type != WaypointType.PASSENGER_PICKUP
                && segEnd.type != WaypointType.PASSENGER_DROPOFF) {
            return SEGMENT_TYPE_MAIN;
        }

        // Count how many distinct passenger waypoints share this endpoint location (within 50 m).
        // If more than one passenger is at this point it is a shared MAIN segment.
        long passengersAtEndpoint = waypoints.stream()
                .filter(wp -> (wp.type == WaypointType.PASSENGER_PICKUP
                        || wp.type == WaypointType.PASSENGER_DROPOFF)
                        && haversineDistance(
                                wp.latitude.doubleValue(), wp.longitude.doubleValue(),
                                segEnd.latitude.doubleValue(), segEnd.longitude.doubleValue()) < 0.05)
                .map(wp -> wp.shareRideDetailId)
                .distinct()
                .count();

        if (passengersAtEndpoint > 1) {
            // Multiple passengers share this waypoint — classify as MAIN so all get charged
            return SEGMENT_TYPE_MAIN;
        }

        // There must be a waypoint after the end to check for backtracking
        int nextIndex = segEndIndex + 2; // segEndIndex is the index of segStart; segEnd is +1
        if (nextIndex >= waypoints.size()) {
            return SEGMENT_TYPE_MAIN;
        }

        Waypoint nextWaypoint = waypoints.get(nextIndex);

        // A true out-and-back detour: the next waypoint returns to within 200 m of segStart.
        double nextToStart = haversineDistance(
                nextWaypoint.latitude.doubleValue(), nextWaypoint.longitude.doubleValue(),
                segStart.latitude.doubleValue(), segStart.longitude.doubleValue());

        return (nextToStart < 0.2) ? SEGMENT_TYPE_SIDE_TRIP : SEGMENT_TYPE_MAIN;
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
     * For the read-back path: determine whether a passenger rode a persisted segment.
     *
     * A passenger is on a segment if the segment starts at or after the passenger's
     * pickup location AND ends at or before the passenger's dropoff location —
     * measured by comparing coordinates with a small tolerance (0.5 km).
     *
     * This avoids the midpoint triangle-inequality approach which breaks when two
     * passengers share identical pickup/dropoff coordinates.
     */
    private boolean isPassengerOnPersistedSegment(ShareRideDetail passenger, RideSegment segment) {
        final double TOLERANCE_KM = 0.5;

        double pickupLat  = passenger.getStartLocationLatitude().doubleValue();
        double pickupLng  = passenger.getStartLocationLongitude().doubleValue();
        double dropoffLat = passenger.getEndLocationLatitude().doubleValue();
        double dropoffLng = passenger.getEndLocationLongitude().doubleValue();

        double segStartLat = segment.getStartLatitude().doubleValue();
        double segStartLng = segment.getStartLongitude().doubleValue();
        double segEndLat   = segment.getEndLatitude().doubleValue();
        double segEndLng   = segment.getEndLongitude().doubleValue();

        // Distance from passenger pickup to segment start
        double pickupToSegStart = haversineDistance(pickupLat, pickupLng, segStartLat, segStartLng);
        // Distance from passenger dropoff to segment end
        double dropoffToSegEnd  = haversineDistance(dropoffLat, dropoffLng, segEndLat, segEndLng);
        // Passenger's straight-line travel distance
        double passengerDist    = haversineDistance(pickupLat, pickupLng, dropoffLat, dropoffLng);

        // The segment's start must be within [pickup ... dropoff] corridor.
        // We accept the segment if:
        //   1. The segment start is at, or "after", the passenger's pickup (within tolerance), AND
        //   2. The segment end is at, or "before", the passenger's dropoff (within tolerance).
        //
        // "At or after pickup" means the distance from pickup to segment-start is < passenger's total distance + tolerance
        // "At or before dropoff" means dropoffToSegEnd < tolerance OR segment end ≈ dropoff
        //
        // Simplest reliable check: segment start is within passenger trip distance from pickup,
        // AND segment end is within passenger trip distance from dropoff.
        boolean startAfterPickup  = pickupToSegStart <= passengerDist + TOLERANCE_KM;
        boolean endBeforeDropoff  = dropoffToSegEnd  <= passengerDist + TOLERANCE_KM;

        // Also ensure segment start is not beyond the passenger dropoff
        double pickupToSegEnd = haversineDistance(pickupLat, pickupLng, segEndLat, segEndLng);
        boolean segEndWithinTrip = pickupToSegEnd <= passengerDist + TOLERANCE_KM;

        return startAfterPickup && endBeforeDropoff && segEndWithinTrip;
    }

    /**
     * Remove duplicate waypoints that are within 100 m of their predecessor,
     * BUT only when they belong to the same passenger or are both driver waypoints.
     *
     * Two passengers who share identical pickup/dropoff coordinates must NOT be merged
     * because the segment algorithm identifies passengers by their specific waypoint
     * object references. Merging them would make one passenger's waypoints invisible,
     * causing the segment to appear as a one-person SIDE_TRIP instead of a shared MAIN.
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
                // Only merge when it is safe: both are driver waypoints, or exact same passenger
                boolean bothDriver = (prev.type == WaypointType.DRIVER_START || prev.type == WaypointType.DRIVER_END)
                        && (curr.type == WaypointType.DRIVER_START || curr.type == WaypointType.DRIVER_END);
                boolean samePassenger = prev.shareRideDetailId != null
                        && prev.shareRideDetailId.equals(curr.shareRideDetailId);

                if (bothDriver) {
                    // Prefer driver waypoints when merging driver duplicates
                    if (curr.type == WaypointType.DRIVER_START || curr.type == WaypointType.DRIVER_END) {
                        result.set(result.size() - 1, curr);
                    }
                } else if (samePassenger) {
                    // Intentionally skip: duplicate waypoint for the same passenger (no-op)
                } else {
                    // Different passengers at the same location — keep both so each is counted
                    result.add(curr);
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

