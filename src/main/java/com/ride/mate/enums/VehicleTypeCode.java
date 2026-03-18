package com.ride.mate.enums;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * VehicleTypeCode Enum
 * Defines vehicle type codes with their day and night per km rates.
 * Rates are structured similar to ride-sharing platforms (PickMe, Uber).
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-03-2026    N/A          N/A          Tishan          Initial Development
 */
@Getter
public enum VehicleTypeCode {

    /**
     * Standard car - economy ride
     * Day rate: 65.00 LKR/km | Night rate: 85.00 LKR/km
     */
    CAR("CAR", new BigDecimal("65.00"), new BigDecimal("85.00")),

    /**
     * Van - larger capacity vehicle
     * Day rate: 90.00 LKR/km | Night rate: 115.00 LKR/km
     */
    VAN("VAN", new BigDecimal("90.00"), new BigDecimal("115.00")),

    /**
     * Tuk-tuk (three-wheeler) - budget ride
     * Day rate: 40.00 LKR/km | Night rate: 55.00 LKR/km
     */
    TUK("TUK", new BigDecimal("40.00"), new BigDecimal("55.00")),

    /**
     * Bike - fastest, budget option
     * Day rate: 30.00 LKR/km | Night rate: 42.00 LKR/km
     */
    BIKE("BIKE", new BigDecimal("30.00"), new BigDecimal("42.00"));

    private final String code;
    private final BigDecimal dayRate;
    private final BigDecimal nightRate;

    VehicleTypeCode(String code, BigDecimal dayRate, BigDecimal nightRate) {
        this.code = code;
        this.dayRate = dayRate;
        this.nightRate = nightRate;
    }
}


