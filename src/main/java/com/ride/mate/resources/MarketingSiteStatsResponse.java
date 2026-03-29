/**
 * MarketingSiteStatsResponse
 * DTO for marketing site statistics (counts of rides completed, active users, verified drivers)
 *
 * @author Copilot
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 29-03-2026    N/A          N/A          Copilot          Initial Development
 */
package com.ride.mate.resources;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarketingSiteStatsResponse {
    private long ridesCompleted;
    private long activeUsers;
    private long verifiedDrivers;
}

