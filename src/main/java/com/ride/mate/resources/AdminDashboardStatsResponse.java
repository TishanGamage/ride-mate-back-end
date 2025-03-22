package com.ride.mate.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Admin Dashboard Stats Response
 * Response payload containing aggregated statistics for the admin dashboard
 *
 * @author RideMate
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 02-04-2026    N/A          N/A          RideMate         Initial Development
 */
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminDashboardStatsResponse {

    private long totalUsers;
    private long activeUsers;
    private long inactiveUsers;
    private long pendingUsers;
    private long totalPassengers;
    private long totalDrivers;
    private long totalAdmins;

    private long pendingDriverApprovals;
    private long approvedDrivers;
    private long rejectedDrivers;

    private long totalRides;
    private long activeRides;
    private long completedRides;
    private long cancelledRides;

    private long totalReports;
    private long pendingReports;
    private long inReviewReports;
    private long resolvedReports;
    private long totalFeedback;

    private long totalPayments;
    private long pendingWithdrawals;
    private long approvedWithdrawals;
    private long rejectedWithdrawals;
}

