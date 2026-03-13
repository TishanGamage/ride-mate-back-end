package com.ride.mate.service.impl;

import com.ride.mate.core.LoginAuthentication;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.User;
import com.ride.mate.domain.VerificationCode;
import com.ride.mate.enums.UserStatus;
import com.ride.mate.enums.YesNo;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.UserRepository;
import com.ride.mate.resources.*;
import com.ride.mate.service.AuthService;
import com.ride.mate.service.VerificationCodeService;
import com.ride.mate.util.DateUtil;
import com.ride.mate.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Auth Service Implementation
 * Implementation of authentication business logic including login, verification, and token refresh
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 09-03-2026    N/A          N/A          Tishan          Initial Development
 * 2 09-03-2026    N/A          N/A          Tishan          Added login and verification methods
 * 3 09-03-2026    N/A          N/A          Tishan          Added userName to JWT token generation
 */
@Slf4j
@Service
@Transactional
public class AuthServiceImpl extends MessagePropertyBase implements AuthService {

    private final UserRepository userRepository;
    private final VerificationCodeService verificationCodeService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final Environment environment;

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    public AuthServiceImpl(UserRepository userRepository,
                          VerificationCodeService verificationCodeService,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          Environment environment) {
        this.userRepository = userRepository;
        this.verificationCodeService = verificationCodeService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.environment = environment;
    }

    @Override
    public VerificationCode sendVerificationCode(SendVerificationCodeRequest request) {
        log.info("Processing send verification code request for email: {}", request.getEmail());
        return verificationCodeService.sendVerificationCode(request);
    }

    @Override
    public SuccessAndErrorDetailsResource verifyCode(VerifyCodeRequest request) {
        log.info("Processing verify code request for email: {}", request.getEmail());
        return verificationCodeService.verifyCode(request);
    }

    @Override
    public LoginResponse loginUser(LoginRequest request) {
        log.info("Processing login request for email: {}", request.getEmail());

        // Validate user exists
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());
        if (optionalUser.isEmpty()) {
            log.warn("Login failed: User not found for email - {}", request.getEmail());
            throw new ValidateRecordException(environment.getProperty(LOGIN_USER_NOT_FOUND), "message");
        }

        User user = optionalUser.get();

        // Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: Invalid credentials for email - {}", request.getEmail());
            throw new ValidateRecordException(environment.getProperty(LOGIN_INVALID_CREDENTIALS), "message");
        }

        // Validate account status
        if (user.getStatus().equals(UserStatus.INACTIVE)) {
            log.warn("Login failed: Account suspended for email - {}", request.getEmail());
            throw new ValidateRecordException(environment.getProperty(LOGIN_ACCOUNT_SUSPENDED), "message");
        }

        // Validate email verification - if not verified, send verification code
        if (user.getEmailVerified() == YesNo.NO) {
            log.warn("Login failed: Email not verified for email - {}", request.getEmail());
            SendVerificationCodeRequest verificationRequest = new SendVerificationCodeRequest();
            verificationRequest.setEmail(request.getEmail());
            verificationCodeService.sendVerificationCode(verificationRequest);
            throw new ValidateRecordException(environment.getProperty(LOGIN_EMAIL_NOT_VERIFIED), "message");
        }

        // Update last login date
        user.setLastLoginDate(DateUtil.getDate());
        user.setModifiedDate(DateUtil.getDate());
        user.setModifiedUser(LoginAuthentication.getUserName());
        userRepository.save(user);

        // Generate JWT tokens
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getUserRole().name(), user.getFirstName());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        // Build response with tokens
        LoginResponse response = LoginResponse.builder()
                .message(environment.getProperty(LOGIN_SUCCESS))
                .success(true)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000) // Convert to seconds
                .userId(user.getId())
                .userName(user.getFirstName())
                .email(user.getEmail())
                .role(user.getUserRole().name())
                .emailVerified(user.getEmailVerified().toString())
                .build();

        log.info("User logged in successfully with ID: {} for email: {}", user.getId(), user.getEmail());
        return response;
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        log.info("Processing token refresh request");

        String refreshToken = request.getRefreshToken();

        // Validate refresh token
        if (!jwtUtil.validateToken(refreshToken)) {
            log.warn("Token refresh failed: Invalid refresh token");
            throw new ValidateRecordException(environment.getProperty(JWT_REFRESH_TOKEN_INVALID), "message");
        }

        // Validate that it's a refresh token
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            log.warn("Token refresh failed: Token is not a refresh token");
            throw new ValidateRecordException(environment.getProperty(JWT_REFRESH_TOKEN_INVALID), "message");
        }

        // Check if token is expired
        if (jwtUtil.isTokenExpired(refreshToken)) {
            log.warn("Token refresh failed: Refresh token expired");
            throw new ValidateRecordException(environment.getProperty(JWT_REFRESH_TOKEN_EXPIRED), "message");
        }

        // Extract user info from refresh token
        String email = jwtUtil.extractEmail(refreshToken);

        // Validate user still exists
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            log.warn("Token refresh failed: User not found for email - {}", email);
            throw new ValidateRecordException(environment.getProperty(JWT_USER_NOT_FOUND), "message");
        }

        User user = optionalUser.get();

        // Generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getUserRole().name(), user.getFirstName());

        // Build response with new access token (keep same refresh token)
        LoginResponse response = LoginResponse.builder()
                .message(environment.getProperty(JWT_TOKEN_REFRESHED))
                .success(true)
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Return same refresh token
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000) // Convert to seconds
                .userId(user.getId())
                .userName(user.getFirstName())
                .email(user.getEmail())
                .role(user.getUserRole().name())
                .build();

        log.info("Token refreshed successfully for user: {}", email);
        return response;
    }
}

