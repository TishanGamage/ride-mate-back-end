package com.ride.mate.resources;

import com.ride.mate.enums.YesNo;
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
 * 3 16-03-2026    N/A          N/A          Tishan          Added user verification image document
 * 4 17-03-2026    N/A          N/A          Tishan          Changed willingToDrive to YesNo enum
 */
@Getter
@Setter
public class UserProfileAddResource {

    @NotNull(message = "{invalid.value}")
    private Long userId;

    private Long profileImageDocumentId;

    private Long userVerificationImageDocumentId;

    private String dateOfBirth;

    private String gender;

    @NotNull(message = "{invalid.value}")
    private YesNo willingToDrive;

    @Valid
    private DriverProfileRequestResource driverDetails;

    @Valid
    private UserIdentificationDetailsRequestResource userIdentificationDetails;

}
