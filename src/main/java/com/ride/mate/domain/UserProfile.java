package com.ride.mate.domain;

import com.ride.mate.core.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * User Profile Entity
 * Stores extended user profile information
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-02-2026    N/A          N/A          Tishan          Initial Development
 * 2 22-02-2026    N/A          N/A          Tishan          Updated to reference User table
 */
@Getter
@Setter
@Entity
@Table(name = "user_profile")
public class UserProfile extends BaseEntity implements Serializable {

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "address_line_1", length = 255)
    private String addressLine1;

    @Column(name = "address_line_2", length = 255)
    private String addressLine2;

    @Column(name = "address_line_3", length = 255)
    private String addressLine3;

    @Column(name = "address_line_4", length = 255)
    private String addressLine4;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "preferred_language", nullable = false, length = 10)
    private String preferredLanguage = "EN";

    @Column(name = "created_date", nullable = false)
    private Timestamp createdDate;

    @Column(name = "created_user", nullable = false, length = 100)
    private String createdUser;

    @Column(name = "modified_date")
    private Timestamp modifiedDate;

    @Column(name = "modified_user", length = 100)
    private String modifiedUser;
}

