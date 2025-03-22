package com.ride.mate.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Admin User Detail Response
 * Detailed response payload for a single user viewed from admin panel
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
public class AdminUserDetailResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String userRole;
    private String status;
    private String emailVerified;
    private String createdDate;
    private String lastLoginDate;
    private String modifiedDate;

    // Driver-specific fields (null for non-drivers)
    private Long driverProfileId;
    private String driverLicenseNumber;
    private String driverLicenseExpiry;
    private String driverLicenseVerified;
    private String accountStatus;
    private String ratingAsDriver;
    private Long totalRidesAsDriver;
    private String totalEarnings;
    private String driverProfileCompleted;

    // Related data counts
    private Long totalReports;
    private Long totalFeedback;
    private Long totalPayments;

    // Recent rides
    private List<AdminRideResponse> recentRides;
}

