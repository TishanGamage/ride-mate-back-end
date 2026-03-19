package com.ride.mate.resources;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * DriverProfileRequestResource
 * Request payload for driver profile details
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 16-03-2026    N/A          N/A          Tishan          Initial Development
 */
@Getter
@Setter
public class DriverProfileRequestResource {

    private String driverLicenseNumber;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "driverLicenseExpiry must be in yyyy-MM-dd format")
    private String driverLicenseExpiry;

    private Long driverLicenseFrontDocumentId;

    private Long driverLicenseBackDocumentId;

    @Valid
    private DriverVehicleDetailsRequestResource vehicleDetails;
}

