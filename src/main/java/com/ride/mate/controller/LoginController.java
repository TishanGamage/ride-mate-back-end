package com.ride.mate.controller;

import com.ride.mate.domain.VerificationCode;
import com.ride.mate.resources.SendVerificationCodeRequest;
import com.ride.mate.resources.SuccessAndErrorDetailsResource;
import com.ride.mate.resources.VerifyCodeRequest;
import com.ride.mate.service.VerificationCodeService;
import jakarta.validation.Valid;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Login Controller
 * REST API endpoints for authentication and email verification
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-02-2026    N/A          N/A          Tishan          Initial Development
 */
@RestController
@RequestMapping(value = "/login")
@CrossOrigin(origins = "*")
public class LoginController {

    private final VerificationCodeService verificationCodeService;
    private final Environment environment;

    public LoginController(VerificationCodeService verificationCodeService,
                          Environment environment) {
        this.verificationCodeService = verificationCodeService;
        this.environment = environment;
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
            VerificationCode verificationCode = verificationCodeService.sendVerificationCode(request);
            if(verificationCode != null) {
                SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource("Verification code sent successfully",verificationCode.getCode());
                return new ResponseEntity<>(response, HttpStatus.OK);
            }
            else{
                SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource("Failed to send verification code");
                return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_CONTENT);
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
        verificationCodeService.verifyCode(request);
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource("Verification successful", "Code verified successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}