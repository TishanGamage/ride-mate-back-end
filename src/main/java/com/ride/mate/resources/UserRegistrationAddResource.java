package com.ride.mate.resources;

import com.ride.mate.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * User Registration Request DTO
 * Request payload for user registration
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 26-02-2026    N/A          N/A          Tishan          Initial Development
 */
@Getter
@Setter
public class UserRegistrationAddResource {

    @NotBlank(message = "{can.not.be.blank}")
    @Email(message = "{email.invalid}")
    private String email;

    @NotBlank(message = "{invalid.value}")
    private String firstName;

    @NotBlank(message = "{invalid.value}")
    private String lastName;

    @NotBlank(message = "{can.not.be.blank}")
    @Pattern(regexp = "^[0-9]{10,20}$", message = "{invalid.value}")
    private String phoneNumber;

    @NotBlank(message = "{invalid.value}")
    private String password;

    @NotNull(message = "{invalid.value}")
    private UserRole userRole;
}