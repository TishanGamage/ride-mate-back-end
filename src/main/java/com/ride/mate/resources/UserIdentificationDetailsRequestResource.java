package com.ride.mate.resources;

import com.ride.mate.enums.YesNo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * User Identification Details Request Resource
 * Request payload for user identification details
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 28-02-2026    N/A          N/A          Tishan          Initial Development
 */
@Getter
@Setter
public class UserIdentificationDetailsRequestResource {

    @NotNull(message = "{invalid.value}")
    private Long userId;

    @NotNull(message = "{invalid.value}")
    private Long identificationTypeId;

    @NotBlank(message = "{can.not.be.blank}")
    private String identificationNumber;

    private Long frontImageDocumentId;

    private Long backImageDocumentId;

    @NotNull(message = "{invalid.value}")
    private YesNo isVerified;

    private String verificationNotes;

    @NotBlank(message = "{can.not.be.blank}")
    private String status;
}
