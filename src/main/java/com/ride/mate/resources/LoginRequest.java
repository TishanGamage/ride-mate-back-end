package com.ride.mate.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 28-02-2026    N/A          N/A          Iruni          Initial Development
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "{can.not.be.blank}")
    private String password;

    @NotBlank(message = "{can.not.be.blank}")
    @Email(message = "{email.invalid}")
    private String email;

}
