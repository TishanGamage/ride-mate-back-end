package com.ride.mate.domain;

import com.ride.mate.core.BaseEntity;
import com.ride.mate.enums.YesNo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Vehicle Make Entity
 * Stores vehicle manufacturers (Toyota, Mercedes-Benz, BMW, etc.)
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
@Table(name = "vehicle_make")
public class VehicleMake extends BaseEntity implements Serializable {

    @Column(name = "make_code", nullable = false, unique = true, length = 50)
    private String makeCode;

    @Column(name = "make_name", nullable = false, length = 100)
    private String makeName;

    @Column(name = "country_of_origin", length = 100)
    private String countryOfOrigin;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "is_active", nullable = false, length = 3)
    private YesNo isActive;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "created_user", nullable = false, length = 100)
    private String createdUser;

    @Column(name = "created_date", nullable = false)
    private Timestamp createdDate;

    @Column(name = "modified_user", length = 100)
    private String modifiedUser;

    @Column(name = "modified_date")
    private Timestamp modifiedDate;
}

