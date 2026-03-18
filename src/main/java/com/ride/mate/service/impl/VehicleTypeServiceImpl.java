package com.ride.mate.service.impl;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.VehicleType;
import com.ride.mate.domain.VehicleTypeRate;
import com.ride.mate.repository.VehicleTypeRateRepository;
import com.ride.mate.repository.VehicleTypeRepository;
import com.ride.mate.service.VehicleTypeService;
import com.ride.mate.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * VehicleTypeServiceImpl
 * Business logic implementation for vehicle type management
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 25-02-2026    N/A          N/A          Iruni           Initial Development
 * 2 17-03-2026    N/A          N/A          Tishan          Updated to follow coding standards
 * 3 18-03-2026    N/A          N/A          Tishan          Added updatePerKmRates for hourly scheduler
 * 4 18-03-2026    N/A          N/A          Tishan          Refactored to read rates from vehicle_type_rate table
 */
@Slf4j
@Service
@Transactional
public class VehicleTypeServiceImpl extends MessagePropertyBase implements VehicleTypeService {

    // Night hours: 20:00 (8 PM) to 05:59 (5:59 AM)
    private static final LocalTime NIGHT_START = LocalTime.of(20, 0);
    private static final LocalTime NIGHT_END = LocalTime.of(6, 0);

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final VehicleTypeRepository vehicleTypeRepository;
    private final VehicleTypeRateRepository vehicleTypeRateRepository;

    public VehicleTypeServiceImpl(VehicleTypeRepository vehicleTypeRepository,
                                  VehicleTypeRateRepository vehicleTypeRateRepository) {
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.vehicleTypeRateRepository = vehicleTypeRateRepository;
    }

    @Override
    public Optional<VehicleType> findById(long id) {
        log.info("Fetching vehicle type by ID: {}", id);
        return vehicleTypeRepository.findById(id);
    }

    @Override
    public List<VehicleType> findByStatus(String status) {
        log.info("Fetching vehicle types by status: {}", status);
        return vehicleTypeRepository.findByStatus(status);
    }

    @Override
    public void updatePerKmRates() {
        LocalTime now = LocalTime.now();
        boolean isNightTime = isNightTime(now);
        log.info("Running per km rate update. Current time: {} | Mode: {}", now, isNightTime ? "NIGHT" : "DAY");

        List<VehicleType> vehicleTypes = vehicleTypeRepository.findByStatus(ACTIVE_STATUS);

        for (VehicleType vehicleType : vehicleTypes) {
            vehicleTypeRateRepository.findByVehicleTypeIdAndStatus(vehicleType.getId(), ACTIVE_STATUS)
                    .ifPresentOrElse(
                            rate -> {
                                vehicleType.setPerKmRate(
                                        isNightTime ? rate.getNightRate() : rate.getDayRate()
                                );
                                vehicleType.setModifiedDate(DateUtil.getDate());
                                vehicleType.setModifiedUser(SYSTEM);
                                vehicleType.setSyncTs(DateUtil.getDate());
                                vehicleTypeRepository.save(vehicleType);
                                log.info("Updated perKmRate for [{}] to {} ({})",
                                        vehicleType.getCode(),
                                        vehicleType.getPerKmRate(),
                                        isNightTime ? "NIGHT" : "DAY");
                            },
                            () -> log.warn("No active rate configuration found for vehicle type [{}] (ID: {}). Skipping rate update.",
                                    vehicleType.getCode(), vehicleType.getId())
                    );
        }

        log.info("Per km rate update completed for all vehicle types.");
    }

    /**
     * Determines whether the given time falls within night hours (20:00 - 05:59).
     *
     * @param time the current local time
     * @return true if night time, false if day time
     */
    private boolean isNightTime(LocalTime time) {
        return time.isAfter(NIGHT_START) || time.isBefore(NIGHT_END);
    }
}
