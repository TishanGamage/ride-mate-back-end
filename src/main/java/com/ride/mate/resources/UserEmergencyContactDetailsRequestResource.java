package com.ride.mate.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * User Emergency Contact Details Request Resource
 * DTO for emergency contact information submission
 *
 * @author IruniA
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 14-03-2026    N/A          N/A          IruniA          Initial Development
 */
@Getter
@Setter
public class UserEmergencyContactDetailsRequestResource {

    @NotBlank(message = "{can.not.be.blank}")
    private String contactName;

    @NotBlank(message = "{can.not.be.blank}")
    @Pattern(regexp = "^[0-9]{10,20}$", message = "{invalid.value}")
    private String contactPhone;

    @NotBlank(message = "{can.not.be.blank}")
    private String relationship;

    @NotBlank(message = "{can.not.be.blank}")
    @Pattern(regexp = "^(YES|NO)$", message = "{yes.no.pattern}")
    private String isDefault;

    @Email(message = "{email.invalid}")
    private String email;

    private String addressLine1;

    private String addressLine2;

    private String addressLine3;

    private String addressLine4;

    private String notes;
}
