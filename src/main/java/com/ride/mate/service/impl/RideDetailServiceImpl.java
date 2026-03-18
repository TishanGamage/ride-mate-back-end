package com.ride.mate.service.impl;

import com.ride.mate.core.LoginAuthentication;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.DriverProfile;
import com.ride.mate.domain.RideDetail;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.DriverProfileRepository;
import com.ride.mate.repository.RideDetailRepository;
import com.ride.mate.resources.RideDetailRequestResource;
import com.ride.mate.service.RideDetailService;
import com.ride.mate.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

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
 */
@Slf4j
@Service
@Transactional
public class RideDetailServiceImpl extends MessagePropertyBase implements RideDetailService {

    private final RideDetailRepository rideDetailRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final Environment environment;

    public RideDetailServiceImpl(RideDetailRepository rideDetailRepository,
                                 DriverProfileRepository driverProfileRepository,
                                 Environment environment) {
        this.rideDetailRepository = rideDetailRepository;
        this.driverProfileRepository = driverProfileRepository;
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

        // Create and populate ride detail
        RideDetail rideDetail = new RideDetail();
        rideDetail.setDriverProfile(driverProfile);
        rideDetail.setStartLocationLongitude(request.getStartLocationLongitude());
        rideDetail.setEndLocationLongitude(request.getEndLocationLongitude());
        rideDetail.setStartLocationLatitude(request.getStartLocationLatitude());
        rideDetail.setEndLocationLatitude(request.getEndLocationLatitude());
        rideDetail.setStartCity(request.getStartCity());
        rideDetail.setAvailableSeats(request.getAvailableSeats());
        rideDetail.setTotalRideDistance(request.getTotalRideDistance());
        rideDetail.setTripRoute(request.getTripRoute());
        rideDetail.setStatus(request.getStatus());

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
}

