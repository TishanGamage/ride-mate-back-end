package com.ride.mate.domain;

import com.ride.mate.core.BaseEntity;
import com.ride.mate.enums.YesNo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * Driver Vehicle Details Entity
 * Stores vehicle information for drivers (supports multiple vehicles per driver)
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-02-2026    N/A          N/A          Tishan          Initial Development
 */
@Getter
@Setter
@Entity
@Table(name = "driver_vehicle_details")
public class DriverVehicleDetails extends BaseEntity implements Serializable {

    @Column(name = "driver_profile_id", nullable = false)
    private Long driverProfileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_type_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_driver_vehicle_type"))
    private VehicleType vehicleType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_make_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_driver_vehicle_make"))
    private VehicleMake vehicleMake;

    @Column(name = "registration_number", nullable = false, unique = true, length = 50)
    private String registrationNumber;

    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "color", nullable = false, length = 50)
    private String color;

    @Column(name = "seats", nullable = false)
    private Integer seats;

    @Column(name = "vehicle_image_url", length = 500)
    private String vehicleImageUrl;

    @Column(name = "registration_certificate_url", length = 500)
    private String registrationCertificateUrl;

    @Column(name = "insurance_number", length = 100)
    private String insuranceNumber;

    @Column(name = "insurance_provider", length = 100)
    private String insuranceProvider;

    @Column(name = "insurance_expiry")
    private LocalDate insuranceExpiry;

    @Column(name = "insurance_document_url", length = 500)
    private String insuranceDocumentUrl;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "is_verified", nullable = false, length = 3)
    private YesNo isVerified;

    @Column(name = "verified_date")
    private Timestamp verifiedDate;

    @Column(name = "verified_by", length = 100)
    private String verifiedBy;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "is_primary", nullable = false, length = 3)
    private YesNo isPrimary;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "is_active", nullable = false, length = 3)
    private YesNo isActive;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "created_user", nullable = false, length = 100)
    private String createdUser;

    @Column(name = "created_date", nullable = false)
    private Timestamp createdDate;

    @Column(name = "modified_user", length = 100)
    private String modifiedUser;

    @Column(name = "modified_date")
    private Timestamp modifiedDate;
}

