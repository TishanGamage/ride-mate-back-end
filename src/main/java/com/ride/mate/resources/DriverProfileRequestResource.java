package com.ride.mate.resources;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "{can.not.be.blank}")
    private String driverLicenseNumber;

    @NotBlank(message = "{can.not.be.blank}")
    private String driverLicenseExpiry;

    private Long driverLicenseFrontDocumentId;

    private Long driverLicenseBackDocumentId;

    @NotNull(message = "{invalid.value}")
    @Valid
    private DriverVehicleDetailsRequestResource vehicleDetails;
}

