package com.ride.mate.service.impl;

import com.ride.mate.core.LoginAuthentication;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.*;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.*;
import com.ride.mate.resources.*;
import com.ride.mate.service.MLService;
import com.ride.mate.service.ShareRideDetailService;
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
 * Share Ride Detail Service Implementation
 * Business logic for managing shared ride pooling and passenger ride requests
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Iruni           Initial Development
 * 2 21-03-2026    N/A          N/A          Tishan           getAvailableRidePools now uses max(60/N,20)% cost;
 *                                                            removed searchNearbyRides and requestSharedRideWithMatching
 */
@Slf4j
@Service
@Transactional
public class ShareRideDetailServiceImpl extends MessagePropertyBase implements ShareRideDetailService {

    private static final String STATUS_ACTIVE    = "ACTIVE";
    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_PENDING   = "PENDING";

    /** Base share % for a single passenger: max(60/N, 20) */
    private static final BigDecimal BASE_SHARE_PCT = new BigDecimal("60");
    private static final BigDecimal MIN_SHARE_PCT  = new BigDecimal("20");

    private final ShareRideDetailRepository shareRideDetailRepository;
    private final RideDetailRepository rideDetailRepository;
    private final UserRepository userRepository;
    private final MLService mlService;
    private final Environment environment;

    public ShareRideDetailServiceImpl(ShareRideDetailRepository shareRideDetailRepository,
                                      RideDetailRepository rideDetailRepository,
                                      UserRepository userRepository,
                                      MLService mlService,
                                      Environment environment) {
        this.shareRideDetailRepository = shareRideDetailRepository;
        this.rideDetailRepository = rideDetailRepository;
        this.userRepository = userRepository;
        this.mlService = mlService;
        this.environment = environment;
    }

    @Override
    public ShareRideDetail joinSharedRide(ShareRideDetailAddResource request) {
        log.info("Processing shared ride join for user ID: {} to ride detail ID: {}",
                request.getUserId(), request.getRideDetailId());

        // Validate ride detail exists and is active
        RideDetail rideDetail = rideDetailRepository.findById(request.getRideDetailId())
                .orElseThrow(() -> {
                    log.warn("Validation failed: Ride detail not found - ID: {}", request.getRideDetailId());
                    return new ValidateRecordException(
                            environment.getProperty(RIDE_NOT_FOUND), "message");
                });

        if (!"ACTIVE".equalsIgnoreCase(rideDetail.getStatus())) {
            log.warn("Validation failed: Ride is not active - ID: {} Status: {}",
                    request.getRideDetailId(), rideDetail.getStatus());
            throw new ValidateRecordException(
                    environment.getProperty(RIDE_NOT_ACTIVE), "message");
        }

        // Validate user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> {
                    log.warn("Validation failed: User not found - ID: {}", request.getUserId());
                    return new ValidateRecordException(
                            environment.getProperty(USER_NOT_FOUND), "message");
                });

        // Validate available seats
        long confirmedPassengers = shareRideDetailRepository
                .countByRideDetailIdAndStatus(request.getRideDetailId(), STATUS_CONFIRMED);
        if (confirmedPassengers >= rideDetail.getAvailableSeats()) {
            log.warn("Validation failed: No available seats for ride ID: {}", request.getRideDetailId());
            throw new ValidateRecordException(
                    environment.getProperty(NO_AVAILABLE_SEATS), "message");
        }

        // Create shared ride detail
        ShareRideDetail shareRideDetail = new ShareRideDetail();
        shareRideDetail.setRideDetail(rideDetail);
        shareRideDetail.setUser(user);
        shareRideDetail.setRequestId(request.getRideRequestId());
        shareRideDetail.setStartLocationLatitude(request.getPassengerStartLat());
        shareRideDetail.setStartLocationLongitude(request.getPassengerStartLng());
        shareRideDetail.setEndLocationLatitude(request.getPassengerEndLat());
        shareRideDetail.setEndLocationLongitude(request.getPassengerEndLng());
        shareRideDetail.setStartCity(request.getStartCity());
        shareRideDetail.setEndCity(request.getEndCity());
        shareRideDetail.setPassengerRideDistance(request.getPassengerRideDistance());

        // Calculate passenger cost (split with other passengers)
        BigDecimal passengerCost = calculatePassengerCost(rideDetail, request.getPassengerRideDistance());
        shareRideDetail.setPassengerCost(passengerCost);

        shareRideDetail.setStatus(STATUS_PENDING);
        shareRideDetail.setCreatedDate(DateUtil.getDate());
        shareRideDetail.setCreatedUser(LoginAuthentication.getUserName());

        ShareRideDetail savedDetail = shareRideDetailRepository.save(shareRideDetail);
        log.info("Shared ride created successfully with ID: {}", savedDetail.getId());

        return savedDetail;
    }

    @Override
    public List<SharedRidePoolResponse> getAvailableRidePools(
            BigDecimal passengerStartLat,
            BigDecimal passengerStartLng,
            BigDecimal passengerEndLat,
            BigDecimal passengerEndLng,
            BigDecimal passengerRideDistance,
            BigDecimal radiusKm) {
        log.info("Searching for available ride pools within {} km radius", radiusKm);

        List<RideDetail> activeRides = rideDetailRepository.findByStatus(STATUS_ACTIVE);

        if (activeRides.isEmpty()) {
            log.info("No active rides found");
            return Collections.emptyList();
        }

        List<RideDetail> candidateRides = activeRides.stream()
                .filter(ride -> isWithinRadius(
                        passengerStartLat, passengerStartLng,
                        ride.getStartLocationLatitude(), ride.getStartLocationLongitude(),
                        radiusKm))
                .filter(this::hasAvailableSeats)
                .collect(Collectors.toList());

        if (candidateRides.isEmpty()) {
            log.info("No candidate rides within radius {} km", radiusKm);
            return Collections.emptyList();
        }

        log.info("Found {} candidate rides, calling ML service for ranking", candidateRides.size());

        if (mlService.isMLServiceAvailable()) {
            return rankRidesByMLPrediction(candidateRides, passengerStartLat, passengerStartLng,
                    passengerEndLat, passengerEndLng, passengerRideDistance);
        } else {
            log.warn("ML service unavailable, returning unranked pool");
            return candidateRides.stream()
                    .map(ride -> buildPoolResponse(ride, passengerRideDistance))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Rank available rides using ML model predictions.
     * Uses passengerRideDistance for accurate cost estimates via max(60/N,20)%.
     */
    private List<SharedRidePoolResponse> rankRidesByMLPrediction(
            List<RideDetail> candidateRides,
            BigDecimal passengerStartLat,
            BigDecimal passengerStartLng,
            BigDecimal passengerEndLat,
            BigDecimal passengerEndLng,
            BigDecimal passengerRideDistance) {

        log.info("ML-based ranking: processing {} rides", candidateRides.size());

        List<MLDriverPredictionRequest.DriverInput> driverInputs = new ArrayList<>();
        Map<String, RideDetail> driverIdToRide = new HashMap<>();

        for (RideDetail ride : candidateRides) {
            try {
                DriverProfile driverProfile = ride.getDriverProfile();
                if (driverProfile == null) continue;

                float routeDeviation = calculateRouteDeviation(ride, passengerStartLat, passengerStartLng);
                int zoneDensity = estimateZoneDensity(ride.getStartLocationLatitude(), ride.getStartLocationLongitude());
                float tripDistance = ride.getTotalRideDistance().floatValue();
                float headingAngle = calculateHeadingAngle(
                        ride.getStartLocationLatitude(), ride.getStartLocationLongitude(),
                        ride.getEndLocationLatitude(), ride.getEndLocationLongitude());

                driverInputs.add(MLDriverPredictionRequest.DriverInput.builder()
                        .driverId(String.valueOf(driverProfile.getId()))
                        .routeDeviationPct((double) routeDeviation)
                        .zoneDensity(zoneDensity)
                        .tripDistanceKm((double) tripDistance)
                        .headingAngleDeg((double) headingAngle)
                        .build());

                driverIdToRide.put(String.valueOf(driverProfile.getId()), ride);
            } catch (Exception e) {
                log.error("Error extracting ML features for ride {}: {}", ride.getId(), e.getMessage());
            }
        }

        if (driverInputs.isEmpty()) {
            return candidateRides.stream()
                    .map(ride -> buildPoolResponse(ride, passengerRideDistance))
                    .collect(Collectors.toList());
        }

        try {
            MLDriverPredictionRequest mlRequest = new MLDriverPredictionRequest();
            mlRequest.setPassengerId("passenger_" + System.currentTimeMillis());
            mlRequest.setDrivers(driverInputs);

            MLDriverPredictionResponse mlResponse = mlService.predictDriverAcceptance(mlRequest);

            if (mlResponse == null || mlResponse.getRankedDrivers() == null
                    || mlResponse.getRankedDrivers().isEmpty()) {
                log.warn("No ML predictions received — returning unranked results");
                return candidateRides.stream()
                        .map(ride -> buildPoolResponse(ride, passengerRideDistance))
                        .collect(Collectors.toList());
            }

            log.info("ML prediction successful. Top driver: {}, acceptance: {}",
                    mlResponse.getTopDriverId(),
                    mlResponse.getRankedDrivers().get(0).getPredictedAcceptanceRate());

            List<SharedRidePoolResponse> rankedResults = new ArrayList<>();
            for (MLDriverPredictionResponse.RankedDriver ranked : mlResponse.getRankedDrivers()) {
                RideDetail ride = driverIdToRide.get(ranked.getDriverId());
                if (ride != null) {
                    SharedRidePoolResponse resp = buildPoolResponse(ride, passengerRideDistance);
                    resp.setMlAcceptanceProbability(ranked.getPredictedAcceptanceRate());
                    resp.setMlRank(ranked.getRank());
                    rankedResults.add(resp);
                }
            }

            log.info("ML ranking complete. Returning {} ranked rides", rankedResults.size());
            return rankedResults;

        } catch (Exception e) {
            log.error("ML service call failed — returning unranked results: {}", e.getMessage(), e);
            return candidateRides.stream()
                    .map(ride -> buildPoolResponse(ride, passengerRideDistance))
                    .collect(Collectors.toList());
        }
    }

    /**
     * Calculate route deviation percentage (0-100)
     * Simulated as deviation from direct passenger route
     */
    private float calculateRouteDeviation(RideDetail ride, BigDecimal passengerLat, BigDecimal passengerLng) {
        try {
            // Calculate direct distance vs ride distance
            double directDistance = calculateDistance(
                    ride.getStartLocationLatitude().doubleValue(),
                    ride.getStartLocationLongitude().doubleValue(),
                    passengerLat.doubleValue(),
                    passengerLng.doubleValue());

            double rideDistance = ride.getTotalRideDistance().doubleValue();

            if (directDistance > 0) {
                float deviation = (float) ((rideDistance - directDistance) / directDistance * 100);
                return Math.max(0, Math.min(100, deviation)); // Clamp 0-100
            }
            return 0;
        } catch (Exception e) {
            log.warn("Error calculating route deviation: {}", e.getMessage());
            return 5.0f; // Default estimate
        }
    }

    /**
     * Estimate zone density based on number of active rides in area
     */
    private int estimateZoneDensity(BigDecimal startLat, BigDecimal startLng) {
        try {
            BigDecimal searchRadius = new BigDecimal("2"); // 2km zone
            long nearbyRides = rideDetailRepository.findByStatus("ACTIVE").stream()
                    .filter(ride -> isWithinRadius(startLat, startLng,
                            ride.getStartLocationLatitude(), ride.getStartLocationLongitude(),
                            searchRadius))
                    .count();

            // Map ride count to density 0-100
            int density = (int) Math.min(100, (nearbyRides * 10));
            log.debug("Zone density calculated: {} (nearby rides: {})", density, nearbyRides);
            return density;
        } catch (Exception e) {
            log.warn("Error calculating zone density: {}", e.getMessage());
            return 50; // Default medium density
        }
    }

    /**
     * Calculate heading angle from start to end coordinates
     * Returns angle in degrees (0-360)
     */
    private float calculateHeadingAngle(BigDecimal startLat, BigDecimal startLng,
                                        BigDecimal endLat, BigDecimal endLng) {
        try {
            double lat1 = Math.toRadians(startLat.doubleValue());
            double lon1 = Math.toRadians(startLng.doubleValue());
            double lat2 = Math.toRadians(endLat.doubleValue());
            double lon2 = Math.toRadians(endLng.doubleValue());

            double dLon = lon2 - lon1;

            double y = Math.sin(dLon) * Math.cos(lat2);
            double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);

            float bearing = (float) Math.toDegrees(Math.atan2(y, x));
            bearing = (bearing + 360) % 360; // Normalize to 0-360

            log.debug("Heading angle calculated: {}°", bearing);
            return bearing;
        } catch (Exception e) {
            log.warn("Error calculating heading angle: {}", e.getMessage());
            return 45.0f; // Default northeast
        }
    }

    @Override
    public List<ShareRideDetailResponse> getRidePassengers(Long rideDetailId) {
        log.info("Fetching passengers for ride ID: {}", rideDetailId);

        return shareRideDetailRepository.findByRideDetailIdAndStatus(rideDetailId, STATUS_CONFIRMED)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShareRideDetailResponse> getPassengerRideHistory(Long userId) {
        log.info("Fetching ride history for user ID: {}", userId);

        return shareRideDetailRepository.findByUserId(userId)
                .stream()
                .filter(detail -> STATUS_COMPLETED.equalsIgnoreCase(detail.getStatus()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ShareRideDetail updateShareRideStatus(Long shareRideDetailId, String status) {
        log.info("Updating shared ride status to: {} for ID: {}", status, shareRideDetailId);

        ShareRideDetail shareRideDetail = shareRideDetailRepository.findById(shareRideDetailId)
                .orElseThrow(() -> {
                    log.warn("Validation failed: Shared ride detail not found - ID: {}", shareRideDetailId);
                    return new ValidateRecordException(
                            environment.getProperty(SHARED_RIDE_NOT_FOUND), "message");
                });

        shareRideDetail.setStatus(status);
        shareRideDetail.setModifiedDate(DateUtil.getDate());
        shareRideDetail.setModifiedUser(LoginAuthentication.getUserName());

        ShareRideDetail updated = shareRideDetailRepository.save(shareRideDetail);
        log.info("Shared ride status updated successfully with ID: {}", updated.getId());

        return updated;
    }

    @Override
    public ShareRideDetail cancelSharedRide(Long shareRideDetailId) {
        log.info("Cancelling shared ride ID: {}", shareRideDetailId);
        return updateShareRideStatus(shareRideDetailId, STATUS_CANCELLED);
    }

    @Override
    public BigDecimal calculatePooledCost(Long rideDetailId) {
        log.info("Calculating pooled cost for ride ID: {}", rideDetailId);

        RideDetail rideDetail = rideDetailRepository.findById(rideDetailId)
                .orElseThrow(() -> new ValidateRecordException(
                        environment.getProperty(RIDE_NOT_FOUND), "message"));

        long confirmedPassengers = shareRideDetailRepository
                .countByRideDetailIdAndStatus(rideDetailId, STATUS_CONFIRMED);

        if (confirmedPassengers == 0) {
            return rideDetail.getTotalRideCost();
        }

        // Split total cost among all confirmed passengers
        BigDecimal costPerPassenger = rideDetail.getTotalRideCost()
                .divide(new BigDecimal(confirmedPassengers), 2, RoundingMode.HALF_UP);

        log.info("Pooled cost calculated: {} per passenger from total: {}",
                costPerPassenger, rideDetail.getTotalRideCost());

        return costPerPassenger;
    }

    @Override
    public ShareRideDetailResponse getSharedRideDetails(Long shareRideDetailId) {
        log.info("Fetching detailed shared ride information for ID: {}", shareRideDetailId);

        ShareRideDetail shareRideDetail = shareRideDetailRepository.findById(shareRideDetailId)
                .orElseThrow(() -> new ValidateRecordException(
                        environment.getProperty(SHARED_RIDE_NOT_FOUND), "message"));

        return mapToResponse(shareRideDetail);
    }

    // ============ Helper Methods ============

    /**
     * Calculate cost for a single passenger based on their ride distance
     */
    private BigDecimal calculatePassengerCost(RideDetail rideDetail, BigDecimal passengerDistance) {
        BigDecimal perKmRate = rideDetail.getPerKmRate();

        if (perKmRate == null || perKmRate.compareTo(BigDecimal.ZERO) <= 0) {
            // Fallback: equal split of total cost
            long confirmed = shareRideDetailRepository
                    .countByRideDetailIdAndStatus(rideDetail.getId(), STATUS_CONFIRMED);
            long totalAfterJoin = confirmed + 1;
            return rideDetail.getTotalRideCost()
                    .divide(new BigDecimal(totalAfterJoin), 2, RoundingMode.HALF_UP);
        }

        // Current ACTIVE+CONFIRMED passengers (before this one joins)
        long currentN = shareRideDetailRepository.countByRideDetailIdAndStatus(
                rideDetail.getId(), STATUS_CONFIRMED);
        long projectedN = currentN + 1;

        // share% = max(60 / projectedN, 20)
        BigDecimal sharePct = BASE_SHARE_PCT
                .divide(BigDecimal.valueOf(projectedN), 4, RoundingMode.HALF_UP)
                .max(MIN_SHARE_PCT)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal segmentCost = passengerDistance.multiply(perKmRate).setScale(2, RoundingMode.HALF_UP);
        return segmentCost.multiply(sharePct)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    /**
     * Build pool response from ride detail.
     * Uses real passengerRideDistance for an accurate cost estimate.
     */
    private SharedRidePoolResponse buildPoolResponse(RideDetail rideDetail,
                                                      BigDecimal passengerRideDistance) {
        SharedRidePoolResponse response = new SharedRidePoolResponse();
        response.setRideDetailId(rideDetail.getId());
        response.setDriverProfileId(rideDetail.getDriverProfile().getId());
        response.setStartCity(rideDetail.getStartCity());
        response.setEndCity(rideDetail.getEndCity());
        response.setStartLocationLatitude(rideDetail.getStartLocationLatitude());
        response.setStartLocationLongitude(rideDetail.getStartLocationLongitude());
        response.setEndLocationLatitude(rideDetail.getEndLocationLatitude());
        response.setEndLocationLongitude(rideDetail.getEndLocationLongitude());
        response.setStartTime(rideDetail.getStartTime());

        long confirmedPassengers = shareRideDetailRepository
                .countByRideDetailIdAndStatus(rideDetail.getId(), STATUS_CONFIRMED);
        response.setCurrentPassengers(confirmedPassengers);
        response.setAvailableSeats(rideDetail.getAvailableSeats() - confirmedPassengers);
        response.setTotalRideDistance(rideDetail.getTotalRideDistance());
        response.setTotalRideCost(rideDetail.getTotalRideCost());
        response.setPerKmRate(rideDetail.getPerKmRate());

        // Accurate cost estimate using the passenger's actual route distance
        BigDecimal estimatedCost = calculatePassengerCost(rideDetail, passengerRideDistance);
        response.setEstimatedCostPerPassenger(estimatedCost);

        response.setDriverRating(rideDetail.getDriverProfile().getRatingAsDriver());
        response.setTotalRidesAsDriver(rideDetail.getDriverProfile().getTotalRidesAsDriver());

        return response;
    }

    /**
     * Check if a point is within a given radius using Haversine distance.
     */
    private boolean isWithinRadius(BigDecimal lat1, BigDecimal lng1,
                                    BigDecimal lat2, BigDecimal lng2,
                                    BigDecimal radiusKm) {
        double distance = calculateDistance(
                lat1.doubleValue(), lng1.doubleValue(),
                lat2.doubleValue(), lng2.doubleValue());
        return distance <= radiusKm.doubleValue();
    }

    /**
     * Check if a ride has available seats (confirmed passengers < total seats).
     */
    private boolean hasAvailableSeats(RideDetail rideDetail) {
        long confirmedPassengers = shareRideDetailRepository
                .countByRideDetailIdAndStatus(rideDetail.getId(), STATUS_CONFIRMED);
        return confirmedPassengers < rideDetail.getAvailableSeats();
    }

    /**
     * Haversine formula to calculate the great-circle distance between two points (in km).
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in kilometres
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Map ShareRideDetail to response DTO
     */
    private ShareRideDetailResponse mapToResponse(ShareRideDetail detail) {
        ShareRideDetailResponse response = new ShareRideDetailResponse();
        response.setId(detail.getId());
        response.setRideDetailId(detail.getRideDetail().getId());
        response.setUserId(detail.getUser().getId());
        response.setUserEmail(detail.getUser().getEmail());

        // Set passenger name - use email if profile not found
        response.setPassengerName(detail.getUser().getEmail());

        response.setPassengerStartLat(detail.getStartLocationLatitude());
        response.setPassengerStartLng(detail.getStartLocationLongitude());
        response.setPassengerEndLat(detail.getEndLocationLatitude());
        response.setPassengerEndLng(detail.getEndLocationLongitude());
        response.setStartCity(detail.getStartCity());
        response.setEndCity(detail.getEndCity());
        response.setPassengerRideDistance(detail.getPassengerRideDistance());
        response.setPassengerCost(detail.getPassengerCost());
        response.setStatus(detail.getStatus());
        response.setCreatedDate(detail.getCreatedDate());
        response.setModifiedDate(detail.getModifiedDate());

        return response;
    }
}
