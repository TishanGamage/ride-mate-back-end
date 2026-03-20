package com.ride.mate.resources;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * ML Driver Prediction Response (DTO)
 * Response payload from ML model with ranked drivers and acceptance rates
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
public class MLDriverPredictionResponse {

    private String passengerId;

    private List<RankedDriver> rankedDrivers;

    private String topDriverId;

    private String modelVersion;

    @Getter
    @Setter
    public static class RankedDriver {
        private Integer rank;
        private String driverId;
        private Double predictedAcceptanceRate;
        private Double routeDeviationPct;
        private Integer zoneDensity;
        private Double tripDistanceKm;
        private Double headingAngleDeg;
    }
}
