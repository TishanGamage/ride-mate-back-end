package com.ride.mate.service;

import com.ride.mate.resources.MLDriverPredictionRequest;
import com.ride.mate.resources.MLDriverPredictionResponse;

/**
 * ML Service Interface
 * Integration with ML model for driver acceptance rate prediction
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Iruni           Initial Development
 */
public interface MLService {

    /**
     * Predict driver acceptance rates for ride matching
     * Calls the ML model API to get driver rankings based on acceptance probability
     *
     * @param request Driver prediction request containing driver features
     * @return ML prediction response with ranked drivers and acceptance rates
     */
    MLDriverPredictionResponse predictDriverAcceptance(MLDriverPredictionRequest request);

    /**
     * Health check for ML service availability
     *
     * @return true if ML service is available, false otherwise
     */
    boolean isMLServiceAvailable();

    /**
     * Get model version information
     *
     * @return Model version string
     */
    String getModelVersion();
}
