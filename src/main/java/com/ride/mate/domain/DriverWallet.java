package com.ride.mate.domain;

import com.ride.mate.core.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * DriverWallet Entity
 * Maintains the driver's wallet balance and aggregated financial summary.
 * Each driver profile has exactly one wallet that tracks available balance,
 * total earnings, total commission deducted, and total withdrawn amounts.
 *
 * @author Danushka
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Danushka          Initial Development
 */
@Getter
@Setter
@Entity
@Table(name = "driver_wallet")
public class DriverWallet extends BaseEntity implements Serializable {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_profile_id", nullable = false, unique = true)
    private DriverProfile driverProfile;

    @Column(name = "available_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(name = "total_earnings", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(name = "total_commission", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCommission = BigDecimal.ZERO;

    @Column(name = "total_withdrawn", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalWithdrawn = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "LKR";

    @Column(name = "created_date", nullable = false)
    private Timestamp createdDate;

    @Column(name = "created_user", nullable = false, length = 100)
    private String createdUser;

    @Column(name = "modified_date")
    private Timestamp modifiedDate;

    @Column(name = "modified_user", length = 100)
    private String modifiedUser;
}

