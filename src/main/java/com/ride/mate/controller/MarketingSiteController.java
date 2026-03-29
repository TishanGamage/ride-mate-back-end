package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.resources.MarketingSiteStatsResponse;
import com.ride.mate.service.MarketingSiteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MarketingSiteController
 * Provides APIs for marketing site statistics (counts of rides completed, active users, verified drivers)
 *
 * @author Copilot
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 29-03-2026    N/A          N/A          Copilot          Initial Development
 */
@Slf4j
@RestController
@RequestMapping(value = "/marketing-site")
@CrossOrigin(origins = "*")
public class MarketingSiteController extends MessagePropertyBase {

    private final MarketingSiteService marketingSiteService;

    public MarketingSiteController(MarketingSiteService marketingSiteService, Environment environment) {
        this.marketingSiteService = marketingSiteService;
    }

    /**
     * Get marketing site statistics (rides completed, active users, verified drivers)
     *
     * @return ResponseEntity with MarketingSiteStatsResponse
     */
    @GetMapping("/stats")
    public ResponseEntity<MarketingSiteStatsResponse> getMarketingSiteStats() {
        log.info("Received request for marketing site statistics");
        MarketingSiteStatsResponse response = marketingSiteService.getMarketingSiteStats();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
