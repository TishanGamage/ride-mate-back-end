package com.ride.mate.service;

import com.ride.mate.domain.VerificationCode;
import com.ride.mate.resources.SendVerificationCodeRequest;
import com.ride.mate.resources.SuccessAndErrorDetailsResource;
import com.ride.mate.resources.VerifyCodeRequest;

/**
 * Verification Code Service Interface
 * Business logic for email verification code operations
 *
 * @author Tishan 
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-02-2026    N/A          N/A          Tishan          Initial Development
 */
public interface VerificationCodeService {

    /**
     * Sends a 6-digit verification code to the specified email
     *
     * @param request the send verification code request
     * @return success message
     */
    VerificationCode sendVerificationCode(SendVerificationCodeRequest request);

    /**
     * Verifies the code entered by the user
     *
     * @param request the verification code request
     * @return SuccessAndErrorDetailsResource with success or error message
     */
    SuccessAndErrorDetailsResource verifyCode(VerifyCodeRequest request);
}

