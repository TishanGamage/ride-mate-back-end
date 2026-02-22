package com.ride.mate.domain;

import com.ride.mate.core.BaseEntity;
import com.ride.mate.enums.YesNo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Set;

/**
 * Driver Profile Entity
 * Stores driver-specific information separate from user profile
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-02-2026    N/A          N/A          Tishan          Initial Development
 * 2 22-02-2026    N/A          N/A          Tishan          Updated to reference User entity
 */
@Getter
@Setter
@Entity
@Table(name = "driver_profile")
public class DriverProfile extends BaseEntity implements Serializable {

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_driver_user"))
    private User user;

    @Column(name = "driver_license_number", nullable = false, unique = true, length = 50)
    private String driverLicenseNumber;

    @Column(name = "driver_license_expiry", nullable = false)
    private LocalDate driverLicenseExpiry;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "driver_license_verified", nullable = false, length = 3)
    private YesNo driverLicenseVerified;

    @Column(name = "driver_license_front_url", length = 500)
    private String driverLicenseFrontUrl;

    @Column(name = "driver_license_back_url", length = 500)
    private String driverLicenseBackUrl;

    @Column(name = "rating_as_driver", nullable = false, precision = 3, scale = 2)
    private BigDecimal ratingAsDriver = BigDecimal.ZERO;

    @Column(name = "total_rides_as_driver", nullable = false)
    private Long totalRidesAsDriver = 0L;

    @Column(name = "total_earnings", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(name = "account_status", nullable = false, length = 20)
    private String accountStatus;

    @Column(name = "approved_by", nullable = false, length = 100)
    private String approvedBy;

    @Column(name = "approved_date")
    private Timestamp approvedDate;

    @Column(name = "created_user", nullable = false, length = 100)
    private String createdUser;

    @Column(name = "created_date", nullable = false)
    private Timestamp createdDate;

    @Column(name = "modified_user", length = 100)
    private String modifiedUser;

    @Column(name = "modified_date")
    private Timestamp modifiedDate;

    // Relationships
    @OneToMany(mappedBy = "driverProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<DriverVehicleDetails> vehicles;
}

