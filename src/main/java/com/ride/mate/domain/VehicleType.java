package com.ride.mate.domain;

import com.ride.mate.core.BaseEntity;
import com.ride.mate.enums.YesNo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Vehicle Type Entity
 * Stores predefined vehicle types (CAR, VAN, BIKE, SUV)
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
@Table(name = "vehicle_type")
public class VehicleType extends BaseEntity implements Serializable {

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String typeCode;

    @Column(name = "name", nullable = false, length = 100)
    private String typeName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "base_fare", precision = 10, scale = 2)
    private BigDecimal baseFare;

    @Column(name = "per_km_rate", precision = 10, scale = 2)
    private BigDecimal perKmRate;

    @Column(name = "min_seats")
    private Integer minSeats;

    @Column(name = "max_seats")
    private Integer maxSeats;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false, length = 3)
    private YesNo status;

    @Column(name = "created_user", nullable = false, length = 100)
    private String createdUser;

    @Column(name = "created_date", nullable = false)
    private Timestamp createdDate;

    @Column(name = "modified_user", length = 100)
    private String modifiedUser;

    @Column(name = "modified_date")
    private Timestamp modifiedDate;
}

