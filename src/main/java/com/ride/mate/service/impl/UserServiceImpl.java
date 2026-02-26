package com.ride.mate.service.impl;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.User;
import com.ride.mate.enums.UserStatus;
import com.ride.mate.enums.YesNo;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.UserRepository;
import com.ride.mate.resources.UserRegistrationAddResource;
import com.ride.mate.service.UserService;
import com.ride.mate.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

/**
 * User Service Implementation *  of user management business logic
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 26-02-2026    N/A          N/A          Tishan          Initial Development
 */
@Slf4j
@Service
@Transactional
public class UserServiceImpl extends MessagePropertyBase implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, Environment environment) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    public User registerUser(UserRegistrationAddResource request) {

        log.info("Processing user registration request for email: {}", request.getEmail());
        // Validate if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already exists - {}", request.getEmail());
            throw new ValidateRecordException(environment.getProperty(EMAIL_ALREADY_EXISTS),"message");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            log.warn("Registration failed: Phone number already exists - {}", request.getPhoneNumber());
            throw new ValidateRecordException(environment.getProperty(PHONE_NUMBER_ALREADY_EXISTS),"message");
        }

        // Create new user entity
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUserRole(request.getUserRole());
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(YesNo.NO);
        user.setCreatedDate(DateUtil.getDate());
        user.setCreatedUser("SYSTEM");

        // Save user to database
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {} for email: {}", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }
}

