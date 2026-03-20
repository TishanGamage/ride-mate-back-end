package com.ride.mate.resources;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * ML Driver Prediction Request (DTO)
 * Request payload for ML driver acceptance rate prediction
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Iruni           Initial Development
 */
@Getter
@Setter
public class MLDriverPredictionRequest {

    private String passengerId;

    private List<DriverInput> drivers;

    @Getter
    @Setter
    @Builder
    public static class DriverInput {
        private String driverId;
        private Double routeDeviationPct;
        private Integer zoneDensity;
        private Double tripDistanceKm;
        private Double headingAngleDeg;
    }
}
