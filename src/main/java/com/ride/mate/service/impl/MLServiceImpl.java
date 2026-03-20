package com.ride.mate.service.impl;

import com.ride.mate.resources.MLDriverPredictionRequest;
import com.ride.mate.resources.MLDriverPredictionResponse;
import com.ride.mate.service.MLService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * ML Service Implementation
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
@Slf4j
@Service
@Transactional
public class MLServiceImpl implements MLService {

    @Value("${ml.service.base-url}")
    private String mlServiceBaseUrl;

    @Value("${ml.service.enabled:true}")
    private boolean mlServiceEnabled;

    @Value("${ml.service.timeout:5000}")
    private int mlServiceTimeout;

    private final RestTemplate restTemplate;

    public MLServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public MLDriverPredictionResponse predictDriverAcceptance(MLDriverPredictionRequest request) {
        log.info("Calling ML service for driver acceptance prediction for passenger ID: {}",
                request.getPassengerId());

        if (!mlServiceEnabled) {
            log.warn("ML service is disabled. Returning null prediction response.");
            return null;
        }

        try {
            String predictEndpoint = mlServiceBaseUrl + "/predict";
            log.debug("ML prediction endpoint: {}", predictEndpoint);

            MLDriverPredictionResponse response = restTemplate.postForObject(
                    predictEndpoint,
                    request,
                    MLDriverPredictionResponse.class
            );

            if (response != null) {
                log.info("ML prediction successful. Top driver ID: {}", response.getTopDriverId());
            }

            return response;
        } catch (RestClientException e) {
            log.error("Error calling ML service for driver acceptance prediction: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean isMLServiceAvailable() {
        if (!mlServiceEnabled) {
            log.debug("ML service is disabled in configuration");
            return false;
        }

        try {
            String healthEndpoint = mlServiceBaseUrl + "/health";
            log.debug("Checking ML service health: {}", healthEndpoint);

            Object healthResponse = restTemplate.getForObject(healthEndpoint, Object.class);

            if (healthResponse != null) {
                log.info("ML service is available and healthy");
                return true;
            }
        } catch (RestClientException e) {
            log.warn("ML service health check failed: {}", e.getMessage());
        }

        return false;
    }

    @Override
    public String getModelVersion() {
        if (!mlServiceEnabled) {
            return "ML_SERVICE_DISABLED";
        }

        try {
            String healthEndpoint = mlServiceBaseUrl + "/health";
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> response = restTemplate.getForObject(
                    healthEndpoint, java.util.Map.class);

            if (response != null && response.containsKey("model_version")) {
                String version = (String) response.get("model_version");
                log.info("ML Model version: {}", version);
                return version;
            }
        } catch (RestClientException e) {
            log.warn("Failed to retrieve model version: {}", e.getMessage());
        }

        return "UNKNOWN";
    }
}
