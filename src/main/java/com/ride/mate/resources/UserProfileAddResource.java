package com.ride.mate.resources;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * UserProfileAddResource
 * Request payload for creating a user profile
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 15-03-2026    N/A          N/A          Tishan          Initial Development
 * 2 15-03-2026    N/A          N/A          Tishan          Added identification and emergency contact details
 */
@Getter
@Setter
public class UserProfileAddResource {

    @NotNull(message = "{invalid.value}")
    private Long userId;

    private Long profileImageDocumentId;

    private String dateOfBirth;

    private String gender;


    @Valid
    private UserIdentificationDetailsRequestResource userIdentificationDetails;

    @Valid
    private UserEmergencyContactDetailsRequestResource emergencyContactDetails;
}
