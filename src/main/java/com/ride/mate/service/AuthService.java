package com.ride.mate.service;

import com.ride.mate.domain.VerificationCode;
import com.ride.mate.resources.*;

/**
 * Auth Service Interface
 * Business logic for authentication operations including login, verification, and token refresh
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 09-03-2026    N/A          N/A          Tishan          Initial Development
 * 2 09-03-2026    N/A          N/A          Tishan          Added login and verification methods
 */
public interface AuthService {

    /**
     * Sends a 6-digit verification code to the specified email
     *
     * @param request the send verification code request
     * @return verification code entity
     */
    VerificationCode sendVerificationCode(SendVerificationCodeRequest request);

    /**
     * Verifies the code entered by the user
     *
     * @param request the verification code request
     * @return SuccessAndErrorDetailsResource with success or error message
     */
    SuccessAndErrorDetailsResource verifyCode(VerifyCodeRequest request);

    /**
     * Authenticate user with email and password
     *
     * @param request login request containing email and password
     * @return login response with JWT tokens
     */
    LoginResponse loginUser(LoginRequest request);

    /**
     * Refresh access token using a valid refresh token
     *
     * @param request refresh token request containing the refresh token
     * @return new login response with fresh access token
     */
    LoginResponse refreshToken(RefreshTokenRequest request);

    SuccessAndErrorDetailsResource resetPassword(ResetPasswordRequest request);
}

