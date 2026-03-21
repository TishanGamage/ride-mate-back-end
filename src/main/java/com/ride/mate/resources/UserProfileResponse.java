package com.ride.mate.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * UserProfileResponse
 * Response payload for user profile retrieval
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 16-03-2026    N/A          N/A          Tishan          Initial Development
 * 2 17-03-2026    N/A          N/A          Tishan          Added willingToDrive field
 */
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {

    private Long id;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String role;
    private String status;
    private String emailVerified;

    // Profile details
    private String dateOfBirth;
    private String gender;
    private String bio;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String addressLine4;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String preferredLanguage;
    private String userProfileCompleted;
    private String willingToDrive;

    // Document references
    private Long profileImageDocumentId;
    private String profileImageUrl;
    private Long userVerificationImageDocumentId;
    private String userVerificationImageUrl;

    // Identification details
    private Long identificationTypeId;
    private String identificationTypeName;
    private String identificationNumber;
    private String identificationFrontImageUrl;
    private String identificationBackImageUrl;

    // Audit fields
    private String createdDate;
    private String modifiedDate;
}

