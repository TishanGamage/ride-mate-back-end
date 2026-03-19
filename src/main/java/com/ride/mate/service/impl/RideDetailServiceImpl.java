package com.ride.mate.service.impl;

import com.ride.mate.core.LoginAuthentication;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.DriverProfile;
import com.ride.mate.domain.DriverVehicleDetails;
import com.ride.mate.domain.RideDetail;
import com.ride.mate.domain.ShareRideDetail;
import com.ride.mate.domain.User;
import com.ride.mate.domain.VehicleType;
import com.ride.mate.enums.YesNo;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.DriverProfileRepository;
import com.ride.mate.repository.DriverVehicleDetailsRepository;
import com.ride.mate.repository.RideDetailRepository;
import com.ride.mate.repository.ShareRideDetailRepository;
import com.ride.mate.repository.UserRepository;
import com.ride.mate.resources.PassengerRideConfirmRequestResource;
import com.ride.mate.resources.PassengerRideConfirmResponse;
import com.ride.mate.resources.RideDetailRequestResource;
import com.ride.mate.resources.RidePriceCalculationResponse;
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
    private final Environment environment;

    public RideDetailServiceImpl(RideDetailRepository rideDetailRepository,
                                 DriverProfileRepository driverProfileRepository,
                                 DriverVehicleDetailsRepository driverVehicleDetailsRepository,
                                 ShareRideDetailRepository shareRideDetailRepository,
                                 UserRepository userRepository,
                                 Environment environment) {
        this.rideDetailRepository = rideDetailRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.driverVehicleDetailsRepository = driverVehicleDetailsRepository;
        this.shareRideDetailRepository = shareRideDetailRepository;
        this.userRepository = userRepository;
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

        // Get driver's primary vehicle to validate available seats
        DriverVehicleDetails vehicleDetails = driverVehicleDetailsRepository
                .findByDriverProfileIdAndIsPrimary(request.getDriverProfileId(), YesNo.YES)
                .orElseThrow(() -> {
                    log.warn("Validation failed: No primary vehicle found for driver profile ID: {}",
                            request.getDriverProfileId());
                    return new ValidateRecordException(
                            environment.getProperty(DRIVER_VEHICLE_NOT_FOUND), "message");
                });

        // Get vehicle type to validate available seats against vehicle type capacity
        VehicleType vehicleType = vehicleDetails.getVehicleType();

        if (vehicleType == null) {
            log.warn("Validation failed: Vehicle type not found for driver vehicle ID: {}",
                    vehicleDetails.getId());
            throw new ValidateRecordException(
                    environment.getProperty(VEHICLE_TYPE_NOT_FOUND), "message");
        }

        // Validate available seats against vehicle type's maximum seats
        Integer vehicleTypeMaxSeats = vehicleType.getMaxSeats();

        // Create and populate ride detail
        RideDetail rideDetail = new RideDetail();
        rideDetail.setDriverProfile(driverProfile);
        rideDetail.setStartLocationLongitude(request.getStartLocationLongitude());
        rideDetail.setEndLocationLongitude(request.getEndLocationLongitude());
        rideDetail.setStartLocationLatitude(request.getStartLocationLatitude());
        rideDetail.setEndLocationLatitude(request.getEndLocationLatitude());
        rideDetail.setStartCity(request.getStartCity());

        // Validate and set available seats
        if (request.getAvailableSeats() != null && vehicleTypeMaxSeats != null && request.getAvailableSeats() > vehicleTypeMaxSeats) {
            log.warn("Validation failed: Requested seats {} exceeds vehicle type capacity {} for driver profile ID: {}",
                    request.getAvailableSeats(), vehicleTypeMaxSeats, request.getDriverProfileId());
            throw new ValidateRecordException(
                    String.format(environment.getProperty(AVAILABLE_SEATS_EXCEEDS_VEHICLE_CAPACITY), vehicleTypeMaxSeats),
                    "message");
        } else {
            rideDetail.setAvailableSeats(request.getAvailableSeats());
        }
        rideDetail.setTotalRideDistance(request.getTotalRideDistance());
        rideDetail.setTripRoute(request.getTripRoute());
        rideDetail.setStatus(request.getStatus());
        rideDetail.setTotalRideCost(request.getTotalRideCost());
        rideDetail.setPerKmRate(vehicleType.getPerKmRate());
        rideDetail.setSyncTs(DateUtil.getDate());

        // Parse and set timestamps
        if (request.getStartTime() != null && !request.getStartTime().isEmpty()) {
            rideDetail.setStartTime(DateUtil.stringToTimeStamp(request.getStartTime()));
        }


        // Set audit fields
        rideDetail.setCreatedDate(DateUtil.getDate());
        rideDetail.setCreatedUser(LoginAuthentication.getUserName());

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
    public PassengerRideConfirmResponse confirmPassengerRide(PassengerRideConfirmRequestResource request) {
        log.info("Processing passenger ride confirmation for ride ID: {} by user ID: {}",
                request.getRideDetailId(), request.getUserId());

        // Validate ride exists
        RideDetail rideDetail = rideDetailRepository.findById(request.getRideDetailId())
                .orElseThrow(() -> {
                    log.warn("Ride not found - ID: {}", request.getRideDetailId());
                    return new ValidateRecordException(environment.getProperty(RIDE_NOT_FOUND), "message");
                });

        // Validate user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> {
                    log.warn("User not found - ID: {}", request.getUserId());
                    return new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message");
                });

        // Check ride is still open
        if (!"ACTIVE".equalsIgnoreCase(rideDetail.getStatus())) {
            log.warn("Ride ID: {} is not available for booking. Status: {}", rideDetail.getId(), rideDetail.getStatus());
            throw new ValidateRecordException(environment.getProperty(RIDE_NOT_AVAILABLE), "message");
        }

        // Check passenger hasn't already joined this ride
        if (shareRideDetailRepository.existsByRideDetailIdAndUserId(rideDetail.getId(), user.getId())) {
            log.warn("User ID: {} has already confirmed ride ID: {}", user.getId(), rideDetail.getId());
            throw new ValidateRecordException(environment.getProperty(PASSENGER_ALREADY_CONFIRMED), "message");
        }

        // Check available seats
        long confirmedCount = shareRideDetailRepository.countByRideDetailIdAndStatus(rideDetail.getId(), "CONFIRMED");
        if (confirmedCount >= rideDetail.getAvailableSeats()) {
            log.warn("Ride ID: {} has no available seats. Confirmed: {}, Available: {}",
                    rideDetail.getId(), confirmedCount, rideDetail.getAvailableSeats());
            throw new ValidateRecordException(environment.getProperty(RIDE_NO_SEATS_AVAILABLE), "message");
        }

        // Create share ride detail
        ShareRideDetail shareRideDetail = new ShareRideDetail();
        shareRideDetail.setRideDetail(rideDetail);
        shareRideDetail.setRequestId(request.getUserId());
        shareRideDetail.setUser(user);
        shareRideDetail.setStartLocationLongitude(request.getStartLocationLongitude());
        shareRideDetail.setEndLocationLongitude(request.getEndLocationLongitude());
        shareRideDetail.setStartCity(request.getStartCity());
        shareRideDetail.setEndCity(request.getEndCity());
        shareRideDetail.setPassengerRideDistance(request.getPassengerRideDistance());
        shareRideDetail.setPassengerCost(request.getPassengerCost());
        shareRideDetail.setStatus("CONFIRMED");
        shareRideDetail.setCreatedDate(DateUtil.getDate());
        shareRideDetail.setCreatedUser(LoginAuthentication.getUserName());
        shareRideDetail.setSyncTs(DateUtil.getDate());

        ShareRideDetail saved = shareRideDetailRepository.save(shareRideDetail);
        log.info("Passenger ride confirmed successfully. ShareRideDetail ID: {}", saved.getId());

        return PassengerRideConfirmResponse.builder()
                .shareRideDetailId(saved.getId())
                .rideDetailId(rideDetail.getId())
                .userId(user.getId())
                .passengerCost(saved.getPassengerCost())
                .passengerRideDistance(saved.getPassengerRideDistance())
                .startCity(saved.getStartCity())
                .endCity(saved.getEndCity())
                .status(saved.getStatus())
                .message(environment.getProperty(RECORD_CREATED))
                .build();
    }
}

