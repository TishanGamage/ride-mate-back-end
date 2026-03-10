package com.ride.mate.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Verify Code Request Resource
 * Request object for verifying the code
 *
 * @author Tishan 
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-02-2026    N/A          N/A          Tishan          Initial Development
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyCodeRequest {

    private Long userId;

    @NotBlank(message = "{email.required}")
    @Email(message = "{email.invalid}")
    private String email;

    @NotBlank(message = "{verification.code.required}")
    @Pattern(regexp = "^[0-9]{6}$", message = "{verification.code.pattern}")
    private String code;
}

