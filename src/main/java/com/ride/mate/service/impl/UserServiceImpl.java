package com.ride.mate.service.impl;

import com.ride.mate.core.LoginAuthentication;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.User;
import com.ride.mate.enums.UserStatus;
import com.ride.mate.enums.YesNo;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.*;
import com.ride.mate.resources.UserRegistrationAddResource;
import com.ride.mate.resources.UserRegistrationUpdateResource;
import com.ride.mate.service.UserService;
import com.ride.mate.util.ConversionUtil;
import com.ride.mate.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User Service Implementation
 * Implementation of user management business logic
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 26-02-2026    N/A          N/A          Tishan          Initial Development
 * 2 02-03-2026    N/A          N/A          Tishan          Updated to use document references
 * 3 09-03-2026    N/A          N/A          Tishan          Moved loginUser to AuthService
 */
@Slf4j
@Service
@Transactional
public class UserServiceImpl extends MessagePropertyBase implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           Environment environment) {
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
            throw new ValidateRecordException(environment.getProperty(EMAIL_ALREADY_EXISTS), "errorMessage");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            log.warn("Registration failed: Phone number already exists - {}", request.getPhoneNumber());
            throw new ValidateRecordException(environment.getProperty(PHONE_NUMBER_ALREADY_EXISTS), "errorMessage");
        }

        // Create new user entity
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUserRole(request.getUserRole());
        user.setStatus(UserStatus.PENDING);
        user.setEmailVerified(YesNo.NO);
        user.setCreatedDate(DateUtil.getDate());
        user.setCreatedUser(LoginAuthentication.getUserName());
        user.setSyncTs(DateUtil.getDate());

        // Save user to database
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {} for email: {}", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }

    @Override
    public User updateUser(UserRegistrationUpdateResource request) {

        User user = userRepository.findById(request.getId()).orElseThrow(() -> new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message"));

        if(request.getVersion() != null && !user.getVersion().equals(ConversionUtil.stringToLong(request.getVersion()))) {
            throw new ValidateRecordException(environment.getProperty(RECORD_VERSION_MISMATCH), "errorMessage");
        }

        if(request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                log.warn("Update failed: Email already exists - {}", request.getEmail());
                throw new ValidateRecordException(environment.getProperty(EMAIL_ALREADY_EXISTS), "errorMessage");
            }
            user.setEmail(request.getEmail());
        }
        if(request.getPhoneNumber() != null && !request.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                log.warn("Update failed: Phone number already exists - {}", request.getPhoneNumber());
                throw new ValidateRecordException(environment.getProperty(PHONE_NUMBER_ALREADY_EXISTS), "errorMessage");
            }
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if(request.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        // Set modified audit fields
        user.setModifiedUser(LoginAuthentication.getUserName());
        user.setModifiedDate(DateUtil.getDate());

        // Save updated user
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", updatedUser.getId());

        return updatedUser;
    }



}

