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

    private Long vehicleTypeId;

    private Long vehicleMakeId;

    private String registrationNumber;

    private String model;

    private Integer year;

    private String color;

    private Integer seats;

    private Long vehicleImageDocumentId;

    private Long registrationCertificateDocumentId;

    private String insuranceNumber;

    private String insuranceProvider;

    private String insuranceExpiry;

    private Long insuranceDocumentId;
}

