/**
 * MarketingSiteService
 * Service interface for marketing site statistics
 *
 * @author Copilot
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 29-03-2026    N/A          N/A          Copilot          Initial Development
 */
package com.ride.mate.service;

import com.ride.mate.resources.MarketingSiteStatsResponse;

public interface MarketingSiteService {
    MarketingSiteStatsResponse getMarketingSiteStats();
}

