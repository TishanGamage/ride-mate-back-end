package com.ride.mate.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DriverProfileResponse
 * Response payload for driver profile retrieval
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 17-03-2026    N/A          N/A          Tishan          Initial Development
 */
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DriverProfileResponse {

    // Driver profile identifiers
    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;

    // License details
    private String driverLicenseNumber;
    private String driverLicenseExpiry;
    private String driverLicenseVerified;

    // License document references
    private Long driverLicenseFrontDocumentId;
    private String driverLicenseFrontDocumentUrl;
    private Long driverLicenseBackDocumentId;
    private String driverLicenseBackDocumentUrl;

    // Stats
    private String ratingAsDriver;
    private Long totalRidesAsDriver;
    private String totalEarnings;

    // Status
    private String accountStatus;
    private String driverProfileCompleted;

    // Vehicles
    private List<DriverVehicleDetailsResponse> vehicles;

    // Audit
    private String createdDate;
    private String modifiedDate;
}

