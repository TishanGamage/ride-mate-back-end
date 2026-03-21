package com.ride.mate.service.impl;

import com.ride.mate.core.LoginAuthentication;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.RideDetail;
import com.ride.mate.domain.RideRequest;
import com.ride.mate.domain.ShareRideDetail;
import com.ride.mate.domain.User;
import com.ride.mate.domain.UserProfile;
import com.ride.mate.enums.YesNo;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.RideDetailRepository;
import com.ride.mate.repository.RideRequestRepository;
import com.ride.mate.repository.ShareRideDetailRepository;
import com.ride.mate.repository.UserProfileRepository;
import com.ride.mate.repository.UserRepository;
import com.ride.mate.repository.DriverProfileRepository;
import com.ride.mate.resources.PassengerEstimatedCostResponse;
import com.ride.mate.resources.RideRequestResource;
import com.ride.mate.resources.RideRequestResponse;
import com.ride.mate.service.CostSplitService;
import com.ride.mate.service.RideRequestService;
import com.ride.mate.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Ride Request Service Implementation
 * Handles the ride request/accept/reject flow between passengers and drivers
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Tishan           Initial Development
 * 2 21-03-2026    N/A          N/A          Tishan           Added cancelRideRequest and estimatePassengerCost
 * 3 21-03-2026    N/A          N/A          Tishan           Removed getAvailableRides (moved to ShareRideDetailService)
 */
@Slf4j
@Service
@Transactional
public class RideRequestServiceImpl extends MessagePropertyBase implements RideRequestService {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_ACCEPTED = "ACCEPTED";
    private static final String STATUS_REJECTED = "REJECTED";

    private final RideRequestRepository rideRequestRepository;
    private final RideDetailRepository rideDetailRepository;
    private final ShareRideDetailRepository shareRideDetailRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final CostSplitService costSplitService;
    private final Environment environment;

    public RideRequestServiceImpl(RideRequestRepository rideRequestRepository,
                                  RideDetailRepository rideDetailRepository,
                                  ShareRideDetailRepository shareRideDetailRepository,
                                  UserRepository userRepository,
                                  UserProfileRepository userProfileRepository,
                                  DriverProfileRepository driverProfileRepository,
                                  CostSplitService costSplitService,
                                  Environment environment) {
        this.rideRequestRepository = rideRequestRepository;
        this.rideDetailRepository = rideDetailRepository;
        this.shareRideDetailRepository = shareRideDetailRepository;
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.costSplitService = costSplitService;
        this.environment = environment;
    }


    @Override
    public RideRequestResponse createRideRequest(RideRequestResource resource) {
        log.info("Creating ride request for ride ID: {}, user ID: {}", resource.getRideDetailId(), resource.getUserId());

        // 1. Validate ride exists and is active
        RideDetail rideDetail = rideDetailRepository.findById(resource.getRideDetailId())
                .orElseThrow(() -> new ValidateRecordException(
                        environment.getProperty(RIDE_DETAIL_NOT_FOUND), "message"));

        if (!STATUS_ACTIVE.equals(rideDetail.getStatus())) {
            throw new ValidateRecordException(
                    environment.getProperty(RIDE_NOT_AVAILABLE), "message");
        }

        // 2. Check available seats
        long currentPassengers = shareRideDetailRepository
                .countByRideDetailIdAndStatus(resource.getRideDetailId(), STATUS_ACTIVE);
        if (rideDetail.getAvailableSeats() != null && currentPassengers >= rideDetail.getAvailableSeats()) {
            throw new ValidateRecordException(
                    environment.getProperty(NO_AVAILABLE_SEATS), "message");
        }

        // 3. Validate user exists
        User user = userRepository.findById(resource.getUserId())
                .orElseThrow(() -> new ValidateRecordException(
                        environment.getProperty(RECORD_NOT_FOUND), "message"));

        // 4. Check no duplicate pending/accepted request
        boolean alreadyRequested = rideRequestRepository.existsByRideDetailIdAndUserIdAndStatusIn(
                resource.getRideDetailId(), resource.getUserId(),
                Arrays.asList(STATUS_PENDING, STATUS_ACCEPTED));
        if (alreadyRequested) {
            throw new ValidateRecordException(
                    environment.getProperty(RIDE_REQUEST_ALREADY_PENDING), "message");
        }

        // 5. Create the ride request
        RideRequest rideRequest = new RideRequest();
        rideRequest.setRideDetail(rideDetail);
        rideRequest.setUser(user);
        rideRequest.setPassengerStartLat(resource.getPassengerStartLat());
        rideRequest.setPassengerStartLng(resource.getPassengerStartLng());
        rideRequest.setPassengerEndLat(resource.getPassengerEndLat());
        rideRequest.setPassengerEndLng(resource.getPassengerEndLng());
        rideRequest.setStartCity(resource.getStartCity());
        rideRequest.setEndCity(resource.getEndCity());
        rideRequest.setPassengerRideDistance(resource.getPassengerRideDistance());
        rideRequest.setStatus(STATUS_PENDING);
        rideRequest.setCreatedDate(DateUtil.getDate());
        rideRequest.setCreatedUser(LoginAuthentication.getUserName());
        rideRequest.setSyncTs(DateUtil.getDate());

        RideRequest saved = rideRequestRepository.save(rideRequest);
        log.info("Ride request created with ID: {}", saved.getId());

        return mapToRideRequestResponse(saved);
    }

    @Override
    public List<RideRequestResponse> getPendingRequestsForDriver(Long driverProfileId) {
        log.info("Fetching pending ride requests for driver profile ID: {}", driverProfileId);

        // Validate driver profile exists
        driverProfileRepository.findById(driverProfileId)
                .orElseThrow(() -> new ValidateRecordException(
                        environment.getProperty(DRIVER_PROFILE_NOT_FOUND), "message"));

        List<RideRequest> pendingRequests = rideRequestRepository
                .findByDriverProfileIdAndStatus(driverProfileId, STATUS_PENDING);

        return pendingRequests.stream()
                .map(this::mapToRideRequestResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RideRequestResponse acceptRideRequest(Long rideRequestId) {
        log.info("Accepting ride request ID: {}", rideRequestId);

        RideRequest rideRequest = rideRequestRepository.findById(rideRequestId)
                .orElseThrow(() -> new ValidateRecordException(
                        environment.getProperty(RIDE_REQUEST_NOT_FOUND), "message"));

        if (!STATUS_PENDING.equals(rideRequest.getStatus())) {
            throw new ValidateRecordException(
                    environment.getProperty(RIDE_REQUEST_ALREADY_PROCESSED), "message");
        }

        RideDetail rideDetail = rideRequest.getRideDetail();

        // Re-check available seats inside the transaction
        long currentPassengers = shareRideDetailRepository
                .countByRideDetailIdAndStatus(rideDetail.getId(), STATUS_ACTIVE);
        if (rideDetail.getAvailableSeats() != null && currentPassengers >= rideDetail.getAvailableSeats()) {
            throw new ValidateRecordException(
                    environment.getProperty(NO_AVAILABLE_SEATS), "message");
        }

        // Update request status to ACCEPTED
        rideRequest.setStatus(STATUS_ACCEPTED);
        rideRequest.setModifiedDate(DateUtil.getDate());
        rideRequest.setModifiedUser(LoginAuthentication.getUserName());
        rideRequestRepository.save(rideRequest);

        // Create the ShareRideDetail (passenger officially joins the ride)
        ShareRideDetail shareRideDetail = new ShareRideDetail();
        shareRideDetail.setRideDetail(rideDetail);
        shareRideDetail.setRequestId(rideRequest.getId());
        shareRideDetail.setUser(rideRequest.getUser());
        shareRideDetail.setStartLocationLatitude(rideRequest.getPassengerStartLat());
        shareRideDetail.setStartLocationLongitude(rideRequest.getPassengerStartLng());
        shareRideDetail.setEndLocationLatitude(rideRequest.getPassengerEndLat());
        shareRideDetail.setEndLocationLongitude(rideRequest.getPassengerEndLng());
        shareRideDetail.setStartCity(rideRequest.getStartCity());
        shareRideDetail.setEndCity(rideRequest.getEndCity());
        shareRideDetail.setPassengerRideDistance(rideRequest.getPassengerRideDistance());
        shareRideDetail.setPassengerCost(BigDecimal.ZERO); // recalculated below
        shareRideDetail.setStatus(STATUS_ACTIVE);
        shareRideDetail.setCreatedDate(DateUtil.getDate());
        shareRideDetail.setCreatedUser(LoginAuthentication.getUserName());
        shareRideDetail.setSyncTs(DateUtil.getDate());

        shareRideDetailRepository.save(shareRideDetail);
        log.info("ShareRideDetail created for accepted request ID: {}", rideRequestId);

        // Recalculate cost split
        costSplitService.calculateCostSplit(rideDetail.getId());

        log.info("Ride request {} accepted, passenger {} joined ride {}",
                rideRequestId, rideRequest.getUser().getId(), rideDetail.getId());

        return mapToRideRequestResponse(rideRequest);
    }

    @Override
    public RideRequestResponse rejectRideRequest(Long rideRequestId) {
        log.info("Rejecting ride request ID: {}", rideRequestId);

        RideRequest rideRequest = rideRequestRepository.findById(rideRequestId)
                .orElseThrow(() -> new ValidateRecordException(
                        environment.getProperty(RIDE_REQUEST_NOT_FOUND), "message"));

        if (!STATUS_PENDING.equals(rideRequest.getStatus())) {
            throw new ValidateRecordException(
                    environment.getProperty(RIDE_REQUEST_ALREADY_PROCESSED), "message");
        }

        rideRequest.setStatus(STATUS_REJECTED);
        rideRequest.setModifiedDate(DateUtil.getDate());
        rideRequest.setModifiedUser(LoginAuthentication.getUserName());
        rideRequestRepository.save(rideRequest);

        log.info("Ride request {} rejected", rideRequestId);

        return mapToRideRequestResponse(rideRequest);
    }

    @Override
    public List<RideRequestResponse> getRequestsByPassenger(Long userId) {
        log.info("Fetching ride requests for user ID: {}", userId);
        List<RideRequest> requests = rideRequestRepository.findByUserId(userId);
        return requests.stream()
                .map(this::mapToRideRequestResponse)
                .collect(Collectors.toList());
    }

    // ─── Helper methods ─────────────────────────────────────────────

    @Override
    public RideRequestResponse cancelRideRequest(Long rideRequestId) {
        log.info("Cancelling ride request ID: {}", rideRequestId);

        RideRequest rideRequest = rideRequestRepository.findById(rideRequestId)
                .orElseThrow(() -> new ValidateRecordException(
                        environment.getProperty(RIDE_REQUEST_NOT_FOUND), "message"));

        // Only PENDING or ACCEPTED requests can be cancelled by the passenger
        if (STATUS_REJECTED.equals(rideRequest.getStatus())
                || "CANCELLED".equals(rideRequest.getStatus())) {
            throw new ValidateRecordException(
                    environment.getProperty(RIDE_REQUEST_ALREADY_PROCESSED), "message");
        }

        boolean wasAccepted = STATUS_ACCEPTED.equals(rideRequest.getStatus());

        // Mark request as cancelled
        rideRequest.setStatus("CANCELLED");
        rideRequest.setModifiedDate(DateUtil.getDate());
        rideRequest.setModifiedUser(LoginAuthentication.getUserName());
        rideRequestRepository.save(rideRequest);

        // If the passenger was already accepted (ShareRideDetail exists), remove them
        // and recalculate cost split for remaining passengers
        if (wasAccepted) {
            List<ShareRideDetail> shareRideDetails = shareRideDetailRepository
                    .findByRideDetailIdAndStatus(rideRequest.getRideDetail().getId(), STATUS_ACTIVE);

            shareRideDetails.stream()
                    .filter(srd -> Objects.equals(srd.getRequestId(), rideRequest.getId()))
                    .findFirst()
                    .ifPresent(srd -> {
                        srd.setStatus("CANCELLED");
                        srd.setModifiedDate(DateUtil.getDate());
                        srd.setModifiedUser(LoginAuthentication.getUserName());
                        shareRideDetailRepository.save(srd);
                        log.info("ShareRideDetail ID {} marked CANCELLED for request ID {}",
                                srd.getId(), rideRequestId);
                    });

            // Recalculate cost for the remaining passengers
            costSplitService.calculateCostSplit(rideRequest.getRideDetail().getId());
            log.info("Cost recalculated after passenger cancelled accepted ride request {}", rideRequestId);
        }

        log.info("Ride request {} cancelled", rideRequestId);
        return mapToRideRequestResponse(rideRequest);
    }

    @Override
    public PassengerEstimatedCostResponse estimatePassengerCost(Long rideDetailId,
                                                                 BigDecimal passengerRideDistance) {
        log.info("Estimating cost for ride ID: {}, passenger distance: {} km",
                rideDetailId, passengerRideDistance);

        RideDetail rideDetail = rideDetailRepository.findById(rideDetailId)
                .orElseThrow(() -> new ValidateRecordException(
                        environment.getProperty(RIDE_DETAIL_NOT_FOUND), "message"));

        if (!STATUS_ACTIVE.equals(rideDetail.getStatus())) {
            throw new ValidateRecordException(
                    environment.getProperty(RIDE_NOT_AVAILABLE), "message");
        }

        BigDecimal perKmRate = rideDetail.getPerKmRate();
        if (perKmRate == null || perKmRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidateRecordException(
                    environment.getProperty(VEHICLE_TYPE_RATE_NOT_CONFIGURED), "message");
        }

        // Current passenger count (ACTIVE in ShareRideDetail)
        int currentPassengers = (int) shareRideDetailRepository
                .countByRideDetailIdAndStatus(rideDetailId, STATUS_ACTIVE);

        // After this passenger joins, N = currentPassengers + 1
        int projectedN = currentPassengers + 1;

        // Share % = max(60 / N, 20)
        BigDecimal sharePct = new BigDecimal("60")
                .divide(BigDecimal.valueOf(projectedN), 4, RoundingMode.HALF_UP)
                .max(new BigDecimal("20"))
                .setScale(2, RoundingMode.HALF_UP);

        // Estimated cost = passengerDistance × perKmRate × (sharePct / 100)
        BigDecimal segmentCost = passengerRideDistance.multiply(perKmRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal estimatedCost = segmentCost
                .multiply(sharePct)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        String note = String.format(
                "You are passenger #%d on this ride. You pay %.0f%% of your segment cost " +
                "(formula: max(60/%d, 20)%%). Driver is a daily commuter — you share running costs only.",
                projectedN, sharePct.doubleValue(), projectedN);

        log.info("Estimated cost for ride {} with {} projected passengers: {} ({}%)",
                rideDetailId, projectedN, estimatedCost, sharePct);

        return PassengerEstimatedCostResponse.builder()
                .rideDetailId(rideDetailId)
                .currentPassengerCount(currentPassengers)
                .projectedPassengerCount(projectedN)
                .perKmRate(perKmRate)
                .passengerRideDistance(passengerRideDistance)
                .sharePercentage(sharePct)
                .estimatedCost(estimatedCost)
                .pricingNote(note)
                .build();
    }

    private RideRequestResponse mapToRideRequestResponse(RideRequest rideRequest) {
        User user = rideRequest.getUser();
        UserProfile profile = userProfileRepository.findByUserId(user.getId()).orElse(null);

        String profileImageUrl = null;
        if (profile != null && profile.getProfileImageDocument() != null) {
            profileImageUrl = profile.getProfileImageDocument().getDocumentUrl();
        }

        // If the request is ACCEPTED, pull the actual calculated cost from ShareRideDetail
        BigDecimal estimatedCost = null;
        if (STATUS_ACCEPTED.equals(rideRequest.getStatus())) {
            List<ShareRideDetail> shareRideDetails = shareRideDetailRepository
                    .findByRideDetailIdAndStatus(rideRequest.getRideDetail().getId(), STATUS_ACTIVE);
            estimatedCost = shareRideDetails.stream()
                    .filter(srd -> Objects.equals(srd.getRequestId(), rideRequest.getId()))
                    .map(ShareRideDetail::getPassengerCost)
                    .findFirst()
                    .orElse(null);
        }

        return RideRequestResponse.builder()
                .id(rideRequest.getId())
                .rideDetailId(rideRequest.getRideDetail().getId())
                .userId(user.getId())
                .passengerFirstName(user.getFirstName())
                .passengerLastName(user.getLastName())
                .passengerEmail(user.getEmail())
                .passengerPhone(user.getPhoneNumber())
                .passengerProfileImageUrl(profileImageUrl)
                .passengerStartLat(rideRequest.getPassengerStartLat())
                .passengerStartLng(rideRequest.getPassengerStartLng())
                .passengerEndLat(rideRequest.getPassengerEndLat())
                .passengerEndLng(rideRequest.getPassengerEndLng())
                .startCity(rideRequest.getStartCity())
                .endCity(rideRequest.getEndCity())
                .passengerRideDistance(rideRequest.getPassengerRideDistance())
                .estimatedCost(estimatedCost)
                .status(rideRequest.getStatus())
                .createdDate(rideRequest.getCreatedDate() != null ? rideRequest.getCreatedDate().toString() : null)
                .build();
    }
}

