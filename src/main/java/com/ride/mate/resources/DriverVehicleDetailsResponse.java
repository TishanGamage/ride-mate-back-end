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
 * 2 18-03-2026    N/A          N/A          Tishan          Added vehicleModelId, multiple vehicle image docs, dual insurance docs, revenue license docs
 */
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DriverVehicleDetailsResponse {

    private Long id;

    // Vehicle type & make & model
    private Long vehicleTypeId;
    private String vehicleTypeName;
    private Long vehicleMakeId;
    private String vehicleMakeName;
    private Long vehicleModelId;
    private String vehicleModelName;

    // Vehicle details
    private String registrationNumber;
    private String model;
    private Integer year;
    private String color;
    private Integer seats;

    // Vehicle images (up to 4)
    private Long vehicleImageDocumentId1;
    private String vehicleImageUrl1;
    private Long vehicleImageDocumentId2;
    private String vehicleImageUrl2;
    private Long vehicleImageDocumentId3;
    private String vehicleImageUrl3;
    private Long vehicleImageDocumentId4;
    private String vehicleImageUrl4;

    // Registration certificate
    private Long registrationCertificateDocumentId;
    private String registrationCertificateUrl;

    // Insurance
    private String insuranceNumber;
    private String insuranceProvider;
    private String insuranceExpiry;
    private Long insuranceDocumentId1;
    private String insuranceDocumentUrl1;
    private Long insuranceDocumentId2;
    private String insuranceDocumentUrl2;

    // Revenue license
    private Long revenueLicenseDocumentId1;
    private String revenueLicenseDocumentUrl1;
    private Long revenueLicenseDocumentId2;
    private String revenueLicenseDocumentUrl2;

    // Status flags
    private String isVerified;
    private String isPrimary;
    private String isActive;
    private String status;

    // Audit
    private String createdDate;
    private String modifiedDate;
}

