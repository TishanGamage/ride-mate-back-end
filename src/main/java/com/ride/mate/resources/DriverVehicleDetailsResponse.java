package com.ride.mate.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DriverVehicleDetailsResponse
 * Response payload for driver vehicle details
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
public class DriverVehicleDetailsResponse {

    private Long id;

    // Vehicle type & make
    private Long vehicleTypeId;
    private String vehicleTypeName;
    private Long vehicleMakeId;
    private String vehicleMakeName;

    // Vehicle details
    private String registrationNumber;
    private String model;
    private Integer year;
    private String color;
    private Integer seats;

    // Vehicle image
    private Long vehicleImageDocumentId;
    private String vehicleImageUrl;

    // Registration certificate
    private Long registrationCertificateDocumentId;
    private String registrationCertificateUrl;

    // Insurance
    private String insuranceNumber;
    private String insuranceProvider;
    private String insuranceExpiry;
    private Long insuranceDocumentId;
    private String insuranceDocumentUrl;

    // Status flags
    private String isVerified;
    private String isPrimary;
    private String isActive;
    private String status;

    // Audit
    private String createdDate;
    private String modifiedDate;
}

