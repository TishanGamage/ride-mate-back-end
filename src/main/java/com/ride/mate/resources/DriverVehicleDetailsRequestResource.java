package com.ride.mate.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DriverVehicleDetailsRequestResource
 * Request payload for driver vehicle details
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
public class DriverVehicleDetailsRequestResource {

    @NotNull(message = "{invalid.value}")
    private Long vehicleTypeId;

    @NotNull(message = "{invalid.value}")
    private Long vehicleMakeId;

    @NotBlank(message = "{can.not.be.blank}")
    private String registrationNumber;

    @NotBlank(message = "{can.not.be.blank}")
    private String model;

    @NotNull(message = "{invalid.value}")
    private Integer year;

    @NotBlank(message = "{can.not.be.blank}")
    private String color;

    @NotNull(message = "{invalid.value}")
    private Integer seats;

    private Long vehicleImageDocumentId;

    private Long registrationCertificateDocumentId;

    private String insuranceNumber;

    private String insuranceProvider;

    private String insuranceExpiry;

    private Long insuranceDocumentId;
}

