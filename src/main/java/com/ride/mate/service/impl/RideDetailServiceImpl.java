package com.ride.mate.service.impl;

import com.ride.mate.core.LoginAuthentication;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.*;
import com.ride.mate.enums.RideSegmentStatus;
import com.ride.mate.enums.RideStatus;
import com.ride.mate.enums.YesNo;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.*;
import com.ride.mate.resources.CostSplitResponse;
import com.ride.mate.resources.PassengerRideConfirmRequestResource;
import com.ride.mate.resources.RideDetailRequestResource;
import com.ride.mate.resources.RideDetailResponseResource;
import com.ride.mate.resources.RidePriceCalculationResponse;
import com.ride.mate.service.CostSplitService;
import com.ride.mate.service.RideDetailService;
import com.ride.mate.util.DateUtil;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Ride Detail Service Implementation
 * Business logic for managing ride details
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 15-03-2026    N/A          N/A          Iruni           Initial Development
 * 2 19-03-2026    N/A          N/A          Iruni           Added calculateRidePrice method
 * 3 20-03-2026    N/A          N/A          Tishan           Added confirmPassengerRide method
 * 4 29-03-2026    N/A          N/A          Tishan          Set all shared ride details and ride segments to INACTIVE when ride ends
 */
@Slf4j
@Service
@Transactional
public class RideDetailServiceImpl extends MessagePropertyBase implements RideDetailService {

    private final RideDetailRepository rideDetailRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final DriverVehicleDetailsRepository driverVehicleDetailsRepository;
    private final ShareRideDetailRepository shareRideDetailRepository;
    private final UserRepository userRepository;
    private final CostSplitService costSplitService;
    private final Environment environment;
    private final RideSegmentRepository rideSegmentRepository;
    private final JavaMailSender mailSender;

    public RideDetailServiceImpl(RideDetailRepository rideDetailRepository,
                                 DriverProfileRepository driverProfileRepository,
                                 DriverVehicleDetailsRepository driverVehicleDetailsRepository,
                                 ShareRideDetailRepository shareRideDetailRepository,
                                 UserRepository userRepository,
                                 CostSplitService costSplitService,
                                 Environment environment,
                                 RideSegmentRepository rideSegmentRepository,
                                 JavaMailSender mailSender) {
        this.rideDetailRepository = rideDetailRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.driverVehicleDetailsRepository = driverVehicleDetailsRepository;
        this.shareRideDetailRepository = shareRideDetailRepository;
        this.userRepository = userRepository;
        this.costSplitService = costSplitService;
        this.environment = environment;
        this.rideSegmentRepository = rideSegmentRepository;
        this.mailSender = mailSender;
    }

    @Override
    public RideDetail createRideDetail(RideDetailRequestResource request) {
        log.info("Processing ride detail creation for driver profile ID: {}", request.getDriverProfileId());

        // Validate driver profile exists
        DriverProfile driverProfile = driverProfileRepository.findById(request.getDriverProfileId())
                .orElseThrow(() -> {
                    log.warn("Validation failed: Driver profile not found - ID: {}", request.getDriverProfileId());
                    return new ValidateRecordException(
                            environment.getProperty(DRIVER_PROFILE_NOT_FOUND), "message");
                });

        if(rideDetailRepository.existsRideDetailByDriverProfileIdAndStatus(request.getDriverProfileId(),"ACTIVE")){
            log.warn("Validation failed: Active ride already exists for driver profile ID: {}",
                    request.getDriverProfileId());
            throw new ValidateRecordException(
                    environment.getProperty(ACTIVE_RIDE_EXISTS), "errorMessage");
        }

        // Create and populate ride detail
        RideDetail rideDetail = new RideDetail();
        rideDetail.setDriverProfile(driverProfile);
        rideDetail.setStartLocationLongitude(request.getStartLocationLongitude());
        rideDetail.setEndLocationLongitude(request.getEndLocationLongitude());
        rideDetail.setStartLocationLatitude(request.getStartLocationLatitude());
        rideDetail.setEndLocationLatitude(request.getEndLocationLatitude());
        rideDetail.setStartCity(request.getStartCity());
        rideDetail.setEndCity(request.getEndCity());
        rideDetail.setAvailableSeats(request.getAvailableSeats());
        rideDetail.setTotalRideDistance(request.getTotalRideDistance());
        rideDetail.setTripRoute(request.getTripRoute());
        rideDetail.setStatus(RideStatus.ACTIVE);

        // Parse and set timestamps
        if (request.getStartTime() != null && !request.getStartTime().isEmpty()) {
            rideDetail.setStartTime(DateUtil.stringToTimeStamp(request.getStartTime()));
        }

        // Set per km rate and total cost if provided
        if (request.getPerKmRate() != null) {
            rideDetail.setPerKmRate(request.getPerKmRate());
        }
        if (request.getTotalRideCost() != null) {
            rideDetail.setTotalRideCost(request.getTotalRideCost());
        }

        // Set audit fields
        rideDetail.setCreatedDate(DateUtil.getDate());
        rideDetail.setCreatedUser(LoginAuthentication.getUserName());
        rideDetail.setSyncTs(DateUtil.getDate());

        // Save to database
        RideDetail savedRideDetail = rideDetailRepository.save(rideDetail);
        log.info("Ride detail created successfully with ID: {}", savedRideDetail.getId());

        return savedRideDetail;
    }

    @Override
    public RidePriceCalculationResponse calculateRidePrice(Long driverProfileId, BigDecimal totalDistance) {
        log.info("Calculating ride price for driver profile ID: {} with distance: {} km",
                driverProfileId, totalDistance);

        // Step 1: Validate driver profile exists
        driverProfileRepository.findById(driverProfileId)
                .orElseThrow(() -> {
                    log.warn("Validation failed: Driver profile not found - ID: {}", driverProfileId);
                    return new ValidateRecordException(
                            environment.getProperty(DRIVER_PROFILE_NOT_FOUND), "message");
                });

        // Step 2: Get driver's primary vehicle details (or any active vehicle)
        DriverVehicleDetails vehicleDetails = driverVehicleDetailsRepository
                .findFirstByDriverProfileIdAndIsPrimary(driverProfileId, YesNo.YES)
                .orElseThrow(() -> {
                    log.warn("Validation failed: No primary vehicle found for driver profile ID: {}",
                            driverProfileId);
                    return new ValidateRecordException(
                            environment.getProperty(DRIVER_VEHICLE_NOT_FOUND), "message");
                });

        // Step 3: Get vehicle type from vehicle details
        VehicleType vehicleType = vehicleDetails.getVehicleType();

        if (vehicleType == null) {
            log.warn("Validation failed: Vehicle type not found for driver vehicle ID: {}",
                    vehicleDetails.getId());
            throw new ValidateRecordException(
                    environment.getProperty(VEHICLE_TYPE_NOT_FOUND), "message");
        }

        // Step 4: Get per km rate from vehicle type
        BigDecimal perKmRate = vehicleType.getPerKmRate();

        if (perKmRate == null || perKmRate.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Validation failed: Per km rate not configured for vehicle type: {}",
                    vehicleType.getName());
            throw new ValidateRecordException(
                    environment.getProperty(VEHICLE_TYPE_RATE_NOT_CONFIGURED), "message");
        }

        // Step 5: Calculate total ride price (distance * per km rate)
        BigDecimal totalRidePrice = totalDistance.multiply(perKmRate);

        log.info("Ride price calculated successfully: {} (Distance: {} km x Rate: {} per km)",
                totalRidePrice, totalDistance, perKmRate);

        // Step 6: Build and return response
        return RidePriceCalculationResponse.builder()
                .driverProfileId(driverProfileId)
                .vehicleTypeId(vehicleType.getId())
                .vehicleTypeName(vehicleType.getName())
                .totalDistance(totalDistance)
                .perKmRate(perKmRate)
                .totalRidePrice(totalRidePrice)
                .build();
    }

    @Override
    public CostSplitResponse confirmPassengerRide(PassengerRideConfirmRequestResource request) {
        log.info("Processing passenger ride confirmation for ride ID: {}, user ID: {}",
                request.getRideDetailId(), request.getUserId());

        // 1. Validate ride detail exists
        RideDetail rideDetail = rideDetailRepository.findById(request.getRideDetailId())
                .orElseThrow(() -> {
                    log.warn("Ride detail not found: {}", request.getRideDetailId());
                    return new ValidateRecordException(
                            environment.getProperty(RIDE_DETAIL_NOT_FOUND), "message");
                });

        // 2. Check available seats
        long currentPassengers = shareRideDetailRepository
                .countByRideDetailIdAndStatus(request.getRideDetailId(), "ACTIVE");
        if (rideDetail.getAvailableSeats() != null && currentPassengers >= rideDetail.getAvailableSeats()) {
            log.warn("No available seats for ride ID: {}", request.getRideDetailId());
            throw new ValidateRecordException(
                    environment.getProperty(NO_AVAILABLE_SEATS), "message");
        }

        // 3. Validate user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> {
                    log.warn("User not found: {}", request.getUserId());
                    return new ValidateRecordException(
                            environment.getProperty(RECORD_NOT_FOUND), "message");
                });

        // 4. Create ShareRideDetail
        ShareRideDetail shareRideDetail = new ShareRideDetail();
        shareRideDetail.setRideDetail(rideDetail);
        shareRideDetail.setRequestId(System.currentTimeMillis()); // unique request ID
        shareRideDetail.setUser(user);
        shareRideDetail.setStartLocationLongitude(request.getStartLocationLongitude());
        shareRideDetail.setStartLocationLatitude(request.getStartLocationLatitude());
        shareRideDetail.setEndLocationLongitude(request.getEndLocationLongitude());
        shareRideDetail.setEndLocationLatitude(request.getEndLocationLatitude());
        shareRideDetail.setStartCity(request.getStartCity());
        shareRideDetail.setEndCity(request.getEndCity());
        shareRideDetail.setPassengerRideDistance(request.getPassengerRideDistance());
        shareRideDetail.setPassengerCost(BigDecimal.ZERO); // will be recalculated
        shareRideDetail.setStatus("ACTIVE");
        shareRideDetail.setCreatedDate(DateUtil.getDate());
        shareRideDetail.setCreatedUser(LoginAuthentication.getUserName());
        shareRideDetail.setSyncTs(DateUtil.getDate());

        shareRideDetailRepository.save(shareRideDetail);
        log.info("Share ride detail created with ID: {}", shareRideDetail.getId());

        // 5. Recalculate cost split for all passengers
        CostSplitResponse costSplit = costSplitService.calculateCostSplit(request.getRideDetailId());

        log.info("Cost split recalculated after passenger {} joined ride {}",
                request.getUserId(), request.getRideDetailId());

        return costSplit;
    }

    @Override
    public RideDetail endRide(Long rideDetailId) {
        log.info("Processing end ride for ride detail ID: {}", rideDetailId);

        RideDetail rideDetail = rideDetailRepository.findById(rideDetailId)
                .orElseThrow(() -> {
                    log.warn("Ride detail not found: {}", rideDetailId);
                    return new ValidateRecordException(
                            environment.getProperty(RIDE_DETAIL_NOT_FOUND), "message");
                });

        rideDetail.setStatus(RideStatus.COMPLETED);
        rideDetail.setModifiedDate(DateUtil.getDate());
        rideDetail.setModifiedUser(LoginAuthentication.getUserName());
        rideDetail.setSyncTs(DateUtil.getDate());

        // Set all related ShareRideDetail records to INACTIVE
        List<ShareRideDetail> shareRideDetails = shareRideDetailRepository.findByRideDetailIdAndStatus(rideDetailId, "ACTIVE");
        for (ShareRideDetail shareRideDetail : shareRideDetails) {
            shareRideDetail.setStatus("INACTIVE");
            shareRideDetail.setModifiedDate(DateUtil.getDate());
            shareRideDetail.setModifiedUser(LoginAuthentication.getUserName());
            shareRideDetail.setSyncTs(DateUtil.getDate());
        }
        shareRideDetailRepository.saveAll(shareRideDetails);
        log.info("Set {} shared ride details to INACTIVE for ride detail ID: {}", shareRideDetails.size(), rideDetailId);

        // Set all related RideSegment records to INACTIVE
        List<RideSegment> rideSegments = rideSegmentRepository.findByRideDetailIdAndStatusOrderBySegmentOrder(rideDetailId, RideSegmentStatus.ACTIVE);
        for (RideSegment rideSegment : rideSegments) {
            rideSegment.setStatus(RideSegmentStatus.INACTIVE);
            rideSegment.setModifiedDate(DateUtil.getDate());
            rideSegment.setModifiedUser(LoginAuthentication.getUserName());
            rideSegment.setSyncTs(DateUtil.getDate());
        }
        rideSegmentRepository.saveAll(rideSegments);
        log.info("Set {} ride segments to INACTIVE for ride detail ID: {}", rideSegments.size(), rideDetailId);

        RideDetail updatedRide = rideDetailRepository.save(rideDetail);
        log.info("Ride ended successfully for ride detail ID: {}", rideDetailId);

        // ── Send ride summary email to driver ──
        try {
            sendRideSummaryEmailToDriver(updatedRide);
        } catch (Exception e) {
            log.error("Failed to send ride summary email to driver: {}", e.getMessage());
        }

        return updatedRide;
    }

    @Override
    public RideDetailResponseResource getActiveRideByDriverProfileId(Long driverProfileId) {
        log.info("Fetching active ride for driver profile ID: {}", driverProfileId);

        List<RideDetail> activeRides = rideDetailRepository
                .findByDriverProfileIdAndStatus(driverProfileId, RideStatus.ACTIVE);

        if (activeRides.isEmpty()) {
            log.warn("No active ride found for driver profile ID: {}", driverProfileId);
            throw new ValidateRecordException(
                    environment.getProperty(RIDE_DETAIL_NOT_FOUND), "message");
        }

        return mapToResponse(activeRides.get(0));
    }

    @Override
    public List<RideDetailResponseResource> getRidesByDriverProfileId(Long driverProfileId, String status) {
        log.info("Fetching rides for driver profile ID: {}, status: {}", driverProfileId, status);

        List<RideDetail> rides = (status != null && !status.isEmpty())
                ? rideDetailRepository.findByDriverProfileIdAndStatus(driverProfileId, RideStatus.valueOf(status))
                : rideDetailRepository.findByDriverProfileId(driverProfileId);

        return rides.stream().map(this::mapToResponse).toList();
    }

    private RideDetailResponseResource mapToResponse(RideDetail ride) {
        return RideDetailResponseResource.builder()
                .id(ride.getId())
                .driverProfileId(ride.getDriverProfile().getId())
                .startLocationLongitude(ride.getStartLocationLongitude())
                .startLocationLatitude(ride.getStartLocationLatitude())
                .endLocationLongitude(ride.getEndLocationLongitude())
                .endLocationLatitude(ride.getEndLocationLatitude())
                .startCity(ride.getStartCity())
                .endCity(ride.getEndCity())
                .availableSeats(ride.getAvailableSeats())
                .startTime(ride.getStartTime() != null ? ride.getStartTime().toString() : null)
                .totalRideDistance(ride.getTotalRideDistance())
                .totalRideCost(ride.getTotalRideCost())
                .perKmRate(ride.getPerKmRate())
                .tripRoute(ride.getTripRoute())
                .status(ride.getStatus().toString())
                .createdDate(ride.getCreatedDate() != null ? ride.getCreatedDate().toString() : null)
                .build();
    }

    /**
     * Send a detailed ride summary email to the driver after ride ends
     * @param rideDetail RideDetail entity
     */
    private void sendRideSummaryEmailToDriver(RideDetail rideDetail) {
        try {
            User driver = rideDetail.getDriverProfile().getUser();
            String to = driver.getEmail();
            String driverName = driver.getFirstName() + " " + driver.getLastName();
            
            CostSplitResponse costSplit = costSplitService.getCostSplit(rideDetail.getId());
            
            String htmlContent = loadRideSummaryTemplate();
            
            htmlContent = htmlContent.replace("{{DRIVER_NAME}}", driverName);
            htmlContent = htmlContent.replace("{{START_CITY}}", rideDetail.getStartCity());
            htmlContent = htmlContent.replace("{{END_CITY}}", rideDetail.getEndCity());
            htmlContent = htmlContent.replace("{{RIDE_ID}}", String.valueOf(rideDetail.getId()));
            htmlContent = htmlContent.replace("{{START_TIME}}", rideDetail.getStartTime() != null ? rideDetail.getStartTime().toString() : "N/A");
            htmlContent = htmlContent.replace("{{END_TIME}}", DateUtil.getDate().toString());
            htmlContent = htmlContent.replace("{{TOTAL_DISTANCE}}", rideDetail.getTotalRideDistance().toString());
            htmlContent = htmlContent.replace("{{PASSENGER_COUNT}}", String.valueOf(costSplit.getTotalPassengers()));
            htmlContent = htmlContent.replace("{{TOTAL_COST}}", costSplit.getTotalRideCost().toString());
            
            StringBuilder passengerRows = new StringBuilder();
            for (CostSplitResponse.PassengerCostDetail p : costSplit.getPassengerCosts()) {
                passengerRows.append("<tr style='border-bottom:1px solid #374151;'>")
                    .append("<td style='color:#e5e7eb !important; font-size:12px; padding:10px 8px;'>").append(p.getUserId()).append("</td>")
                    .append("<td style='color:#d1d5db !important; font-size:12px; padding:10px 8px;'>").append(p.getStartCity()).append("</td>")
                    .append("<td style='color:#d1d5db !important; font-size:12px; padding:10px 8px;'>").append(p.getEndCity()).append("</td>")
                    .append("<td style='color:#d1d5db !important; font-size:12px; padding:10px 8px; text-align:right;'>").append(p.getPassengerRideDistance()).append(" km</td>")
                    .append("<td style='color:#10b981 !important; font-size:12px; padding:10px 8px; text-align:right;'><strong>Rs. ").append(p.getTotalPassengerCost()).append("</strong></td>")
                    .append("</tr>");
            }
            htmlContent = htmlContent.replace("{{PASSENGER_ROWS}}", passengerRows.toString());
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Your Ride Summary - RideMate");
            helper.setText(htmlContent, true);
            
            ClassPathResource logoResource = new ClassPathResource("assets/ride-mate-logo-dark.png");
            helper.addInline("logo", logoResource);
            
            mailSender.send(mimeMessage);
            log.info("Ride summary email sent to driver {} for ride {}", to, rideDetail.getId());
        } catch (Exception e) {
            log.error("Failed to send ride summary email: {}", e.getMessage());
        }
    }
    
    private String loadRideSummaryTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/ride-summary-email.html");
        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }
}
