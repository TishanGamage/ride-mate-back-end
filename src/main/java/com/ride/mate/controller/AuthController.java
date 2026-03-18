package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.VerificationCode;
import com.ride.mate.resources.*;
import com.ride.mate.service.AuthService;
import com.ride.mate.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Auth Controller
 * REST API endpoints for authentication operations including login, verification, and token refresh
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 09-03-2026    N/A          N/A          Tishan          Initial Development
 * 2 09-03-2026    N/A          N/A          Tishan          Added login and verification endpoints
 * 3 09-03-2026    N/A          N/A          Tishan          Added user registration endpoint
 */
@Slf4j
@RestController
@RequestMapping(value = "/auth")
@CrossOrigin(origins = "*")
public class AuthController extends MessagePropertyBase {

    private final AuthService authService;
    private final UserService userService;
    private final Environment environment;

    public AuthController(AuthService authService, UserService userService, Environment environment) {
        this.authService = authService;
        this.userService = userService;
        this.environment = environment;
    }
    /**
     * Register a new user
     * Creates a new user record and returns JWT tokens
     *
     * @param request user registration request containing email, phone, password, and role
     * @return ResponseEntity with LoginResponse containing user info and JWT tokens
     */
    @PostMapping(value = "/register")
    public ResponseEntity<LoginResponse> registerUser(@Valid @RequestBody UserRegistrationAddResource request) {
        log.info("Received user registration request for email: {}", request.getEmail());
        LoginResponse response = userService.registerUser(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Send verification code to email
     * Generates and sends a 6-digit verification code to the provided email address
     *
     * @param request the send verification code request containing email
     * @return ResponseEntity with success or error details
     */
    @PostMapping(value = "/send-verification-code")
    public ResponseEntity<?> sendVerificationCode(@Valid @RequestBody SendVerificationCodeRequest request) {
        log.info("Received send verification code request for email: {}", request.getEmail());
        VerificationCode verificationCode = authService.sendVerificationCode(request);
        if (verificationCode != null) {
            SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource(
                    environment.getProperty(VERIFICATION_CODE_SENT_SUCCESS), verificationCode.getCode());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource(
                    environment.getProperty(VERIFICATION_CODE_SENT_FAILED));
            return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    /**
     * Verify the code entered by user
     * Validates the 6-digit verification code against the one sent to the email
     *
     * @param request the verify code request containing email and code
     * @return ResponseEntity with verification result
     */
    @PostMapping(value = "/verify-code")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        log.info("Received verify code request for email: {}", request.getEmail());
        log.debug("Request reached AuthController.verifyCode method");
        try {
            SuccessAndErrorDetailsResource response = authService.verifyCode(request);
            if (response != null && response.getIsValid()) {
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Error in verifyCode: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * User login with email and password
     * Authenticates user and returns JWT tokens
     *
     * @param request the login request containing email and password
     * @return ResponseEntity with login response containing JWT tokens
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Received login request for email: {}", request.getEmail());
        LoginResponse response = authService.loginUser(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Refresh access token using refresh token
     * Generates a new access token when the current one expires
     *
     * @param request the refresh token request containing the refresh token
     * @return ResponseEntity with new access token
     */
    @PostMapping(value = "/refresh-token")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Received token refresh request");
        LoginResponse response = authService.refreshToken(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/reset-password")
    public ResponseEntity<SuccessAndErrorDetailsResource> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Received reset password request for email: {}", request.getEmail());
        SuccessAndErrorDetailsResource response = authService.resetPassword(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}