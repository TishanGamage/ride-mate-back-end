package com.ride.mate.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Send Verification Code Request Resource
 * Request object for sending verification code
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
public class SendVerificationCodeRequest {

    @NotBlank(message = "{email.required}")
    @Email(message = "{email.invalid}")
    private String email;

    // Optional: used in step 2 (inbox verification) to link back to the target user's email
    private String targetEmail;
}

