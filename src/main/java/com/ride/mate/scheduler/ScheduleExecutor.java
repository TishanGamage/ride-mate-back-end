package com.ride.mate.scheduler;

import com.ride.mate.service.VehicleTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ScheduleExecutor
 * Centralized scheduler for all time-based background tasks in RideMate.
 * Handles periodic operations such as dynamic per km rate updates for vehicle types
 * based on time-of-day (day/night pricing) similar to PickMe and Uber surge logic.
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-03-2026    N/A          N/A          Tishan          Initial Development
 */
@Slf4j
@Component
public class ScheduleExecutor {

    private final VehicleTypeService vehicleTypeService;

    public ScheduleExecutor(VehicleTypeService vehicleTypeService) {
        this.vehicleTypeService = vehicleTypeService;
    }

    /**
     * Hourly per km rate updater for all vehicle types.
     * Runs at the start of every hour (e.g., 00:00, 01:00, 02:00 ...).
     * Applies night rates between 20:00 – 05:59 and day rates from 06:00 – 19:59.
     *
     * <p>Rates are read from the {@code vehicle_type_rate} table in the database.
     * To change rates, update the {@code day_rate} and {@code night_rate} columns
     * directly in the database — no code changes required.</p>
     *
     * Cron expression: {@code 0 0 * * * *} → at second 0, minute 0 of every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    public void updateVehicleTypePerKmRates() {
        log.info("=== [ScheduleExecutor] Hourly per km rate update triggered ===");
        try {
            vehicleTypeService.updatePerKmRates();
            log.info("=== [ScheduleExecutor] Per km rate update completed successfully ===");
        } catch (Exception e) {
            log.error("=== [ScheduleExecutor] Per km rate update failed: {} ===", e.getMessage(), e);
        }
    }
}

