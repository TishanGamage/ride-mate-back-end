/**
 * MarketingSiteServiceImpl
 * Service implementation for marketing site statistics
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 29-03-2026    N/A          N/A          Tishan          Initial Development
 */
package com.ride.mate.service.impl;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.enums.DriverStatus;
import com.ride.mate.enums.RideStatus;
import com.ride.mate.enums.UserStatus;
import com.ride.mate.repository.DriverProfileRepository;
import com.ride.mate.repository.RideDetailRepository;
import com.ride.mate.repository.UserRepository;
import com.ride.mate.resources.MarketingSiteStatsResponse;
import com.ride.mate.service.MarketingSiteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class MarketingSiteServiceImpl extends MessagePropertyBase implements MarketingSiteService {

    private final RideDetailRepository rideDetailRepository;
    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;

    public MarketingSiteServiceImpl(RideDetailRepository rideDetailRepository,
                                    UserRepository userRepository,
                                    DriverProfileRepository driverProfileRepository) {
        this.rideDetailRepository = rideDetailRepository;
        this.userRepository = userRepository;
        this.driverProfileRepository = driverProfileRepository;
    }

    @Override
    public MarketingSiteStatsResponse getMarketingSiteStats() {
        log.info("Processing marketing site statistics fetch");
        long ridesCompleted = rideDetailRepository.countByStatus(RideStatus.COMPLETED);
        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        long verifiedDrivers = driverProfileRepository.countByAccountStatus(DriverStatus.APPROVED);
        MarketingSiteStatsResponse response = new MarketingSiteStatsResponse();
        response.setRidesCompleted(ridesCompleted);
        response.setActiveUsers(activeUsers);
        response.setVerifiedDrivers(verifiedDrivers);
        log.info("Marketing site statistics fetched: ridesCompleted={}, activeUsers={}, verifiedDrivers={}", ridesCompleted, activeUsers, verifiedDrivers);
        return response;
    }
}

