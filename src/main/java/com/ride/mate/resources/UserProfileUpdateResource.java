package com.ride.mate.resources;

import com.ride.mate.enums.YesNo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * UserProfileUpdateResource
 * Request payload for updating a user profile
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 15-03-2026    N/A          N/A          Tishan          Initial Development
 * 2 16-03-2026    N/A          N/A          Tishan          Added user verification image document
 * 3 16-03-2026    N/A          N/A          Tishan          Added willingToDrive and driverDetails fields
 * 4 17-03-2026    N/A          N/A          Tishan          Changed willingToDrive to YesNo enum
 */
@Getter
@Setter
public class UserProfileUpdateResource {


    private Long version;

    private Long profileImageDocumentId;

    private Long userVerificationImageDocumentId;

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

    private YesNo willingToDrive;

    @Valid
    private DriverProfileRequestResource driverDetails;

    @Valid
    private UserIdentificationDetailsRequestResource userIdentificationDetails;

    @Valid
    private UserEmergencyContactDetailsRequestResource emergencyContactDetails;
}

