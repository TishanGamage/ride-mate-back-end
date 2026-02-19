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
 * User Identification Details Entity
 * Stores user identification information (NIC, Passport, etc.)
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
@Table(name = "user_identification_details",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_identification_unique",
                columnNames = {"user_profile_id", "identification_type_id", "identification_number"}
        ))
public class UserIdentificationDetails extends BaseEntity implements Serializable {

    @Column(name = "user_profile_id", nullable = false)
    private Long userProfileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identification_type_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_identification_type"))
    private IdentificationType identificationType;

    @Column(name = "identification_number", nullable = false, length = 100)
    private String identificationNumber;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "issuing_country", length = 100)
    private String issuingCountry;

    @Column(name = "issuing_authority", length = 255)
    private String issuingAuthority;

    @Column(name = "front_image_url", length = 500)
    private String frontImageUrl;

    @Column(name = "back_image_url", length = 500)
    private String backImageUrl;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "is_verified", nullable = false, length = 3)
    private YesNo isVerified;

    @Column(name = "verified_date")
    private Timestamp verifiedDate;

    @Column(name = "verified_by", length = 100)
    private String verifiedBy;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "is_primary", nullable = false, length = 3)
    private YesNo isPrimary;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_user", nullable = false, length = 100)
    private String createdUser;

    @Column(name = "created_date", nullable = false)
    private Timestamp createdDate;

    @Column(name = "modified_user", length = 100)
    private String modifiedUser;

    @Column(name = "modified_date")
    private Timestamp modifiedDate;
}

