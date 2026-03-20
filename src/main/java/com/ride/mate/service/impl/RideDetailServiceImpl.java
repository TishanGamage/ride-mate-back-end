package com.ride.mate.service.impl;

import com.ride.mate.core.LoginAuthentication;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.*;
import com.ride.mate.enums.YesNo;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.*;
import com.ride.mate.resources.CostSplitResponse;
import com.ride.mate.resources.PassengerRideConfirmRequestResource;
import com.ride.mate.resources.RideDetailRequestResource;
import com.ride.mate.resources.RidePriceCalculationResponse;
import com.ride.mate.service.CostSplitService;
import com.ride.mate.service.RideDetailService;
import com.ride.mate.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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

    public RideDetailServiceImpl(RideDetailRepository rideDetailRepository,
                                 DriverProfileRepository driverProfileRepository,
                                 DriverVehicleDetailsRepository driverVehicleDetailsRepository,
                                 ShareRideDetailRepository shareRideDetailRepository,
                                 UserRepository userRepository,
                                 CostSplitService costSplitService,
                                 Environment environment) {
        this.rideDetailRepository = rideDetailRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.driverVehicleDetailsRepository = driverVehicleDetailsRepository;
        this.shareRideDetailRepository = shareRideDetailRepository;
        this.userRepository = userRepository;
        this.costSplitService = costSplitService;
        this.environment = environment;
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
        rideDetail.setStatus(request.getStatus());

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
                .findByDriverProfileIdAndIsPrimary(driverProfileId, YesNo.YES)
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
}

