package com.ride.mate.domain;

import com.ride.mate.core.BaseEntity;
import com.ride.mate.enums.YesNo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Emergency Contact Entity
 * Stores emergency contact information for users
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 22-02-2026    N/A          N/A          Tishan          Initial Development
 */
@Getter
@Setter
@Entity
@Table(name = "emergency_contacts")
public class EmergencyContact extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "contact_name", nullable = false, length = 200)
    private String contactName;

    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;

    @Column(name = "relationship", length = 50)
    private String relationship;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_default", nullable = false, length = 3)
    private YesNo isDefault = YesNo.NO;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "address_line_1", length = 255)
    private String addressLine1;

    @Column(name = "address_line_2", length = 255)
    private String addressLine2;

    @Column(name = "address_line_3", length = 255)
    private String addressLine3;

    @Column(name = "address_line_4", length = 255)
    private String addressLine4;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_date", nullable = false)
    private Timestamp createdDate;

    @Column(name = "created_user", nullable = false, length = 100)
    private String createdUser;

    @Column(name = "modified_date")
    private Timestamp modifiedDate;

    @Column(name = "modified_user", length = 100)
    private String modifiedUser;
}

