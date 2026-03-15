package com.ride.mate.service.impl;

import com.ride.mate.core.LoginAuthentication;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.EmergencyContact;
import com.ride.mate.domain.User;
import com.ride.mate.domain.UserIdentificationDetails;
import com.ride.mate.enums.UserStatus;
import com.ride.mate.enums.YesNo;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.*;
import com.ride.mate.resources.UserEmergencyContactDetailsRequestResource;
import com.ride.mate.resources.UserIdentificationDetailsRequestResource;
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

import java.time.LocalDate;

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
    private final UserIdentificationDetailsRepository userIdentificationDetailsRepository;
    private final IdentificationTypeRepository identificationTypeRepository;
    private final DocumentDetailsRepository documentDetailsRepository;
    private final EmergencyContactRepository emergencyContactRepository;

    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public UserServiceImpl(UserRepository userRepository,
                           UserIdentificationDetailsRepository userIdentificationDetailsRepository,
                           IdentificationTypeRepository identificationTypeRepository,
                           DocumentDetailsRepository documentDetailsRepository, EmergencyContactRepository emergencyContactRepository,
                           PasswordEncoder passwordEncoder,
                           Environment environment) {
        this.userRepository = userRepository;
        this.userIdentificationDetailsRepository = userIdentificationDetailsRepository;
        this.identificationTypeRepository = identificationTypeRepository;
        this.documentDetailsRepository = documentDetailsRepository;
        this.emergencyContactRepository = emergencyContactRepository;
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
            throw new ValidateRecordException(environment.getProperty(RECORD_VERSION_MISMATCH), "message");
        }

        if(request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                log.warn("Update failed: Email already exists - {}", request.getEmail());
                throw new ValidateRecordException(environment.getProperty(EMAIL_ALREADY_EXISTS),"message");
            }
            user.setEmail(request.getEmail());
        }
        if(request.getPhoneNumber() != null && !request.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                log.warn("Update failed: Phone number already exists - {}", request.getPhoneNumber());
                throw new ValidateRecordException(environment.getProperty(PHONE_NUMBER_ALREADY_EXISTS),"message");
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

        // Handle user identification details if provided
        if(request.getUserIdentificationDetails() != null) {
            setUserIdentificationDetails(updatedUser, request);
        }

        //Handle emergency contact details if provided
        if(request.getEmergencyContactDetailsRequestResource() != null) {
            setEmergencyContactDetails(updatedUser, request);
        }

        return updatedUser;
    }


    private void setUserIdentificationDetails(User user, UserRegistrationUpdateResource request) {
        if(request.getUserIdentificationDetails() != null) {
            UserIdentificationDetailsRequestResource detailsRequest = request.getUserIdentificationDetails();

            // Find or create UserIdentificationDetails
            UserIdentificationDetails details = userIdentificationDetailsRepository
                    .findByUserIdAndIdentificationTypeId(user.getId(), detailsRequest.getIdentificationTypeId())
                    .orElse(new UserIdentificationDetails());

            // Set User relationship
            details.setUser(user);

            // Set IdentificationType relationship
            identificationTypeRepository.findById(detailsRequest.getIdentificationTypeId())
                    .ifPresentOrElse(details::setIdentificationType, () -> {
                        throw new ValidateRecordException(environment.getProperty(IDENTIFICATION_TYPE_NOT_FOUND), "message");
                    });

            // Map all fields from request resource to domain entity
            details.setIdentificationNumber(detailsRequest.getIdentificationNumber());
            details.setIssueDate(DateUtil.stringToLocalDate(detailsRequest.getIssueDate()));

            LocalDate expiryDate = DateUtil.stringToLocalDate(detailsRequest.getExpiryDate());
            if(DateUtil.isFutureLocalDateTime(expiryDate)){
                details.setExpiryDate(expiryDate);
            }else{
                log.warn("Validation failed: Expiry date must be a future date - {}", detailsRequest.getExpiryDate());
                throw new ValidateRecordException(environment.getProperty(EXPIRY_DATE_MUST_BE_FUTURE), "message");
            }

            details.setIssuingCountry(detailsRequest.getIssuingCountry());
            details.setIssuingAuthority(detailsRequest.getIssuingAuthority());

            // Set document references if provided
            if (detailsRequest.getFrontImageDocumentId() != null) {
                documentDetailsRepository.findById(detailsRequest.getFrontImageDocumentId())
                    .ifPresent(details::setFrontImageDocument);
            }
            if (detailsRequest.getBackImageDocumentId() != null) {
                documentDetailsRepository.findById(detailsRequest.getBackImageDocumentId())
                    .ifPresent(details::setBackImageDocument);
            }

            details.setIsVerified(detailsRequest.getIsVerified());
            details.setVerificationNotes(detailsRequest.getVerificationNotes());
            details.setStatus(detailsRequest.getStatus());

            // Set audit fields
            if(details.getId() == null) {
                details.setCreatedUser(LoginAuthentication.getUserName());
                details.setCreatedDate(DateUtil.getDate());
            } else {
                details.setModifiedUser(LoginAuthentication.getUserName());
                details.setModifiedDate(DateUtil.getDate());
            }

            // Save the identification details
            userIdentificationDetailsRepository.save(details);
            log.info("User identification details saved for user ID: {}", user.getId());
        }
    }

    private void setEmergencyContactDetails(User user, UserRegistrationUpdateResource request) {
        if(request.getEmergencyContactDetailsRequestResource() != null) {
            UserEmergencyContactDetailsRequestResource contactRequest = request.getEmergencyContactDetailsRequestResource();

            // Find existing emergency contact for this user or create new one
            EmergencyContact contact = emergencyContactRepository
                    .findByUserIdAndIsDefault(user.getId(), YesNo.valueOf(contactRequest.getIsDefault()))
                    .orElse(new EmergencyContact());

            // Set User relationship
            contact.setUser(user);

            // Map all fields from request resource
            contact.setContactName(contactRequest.getContactName());
            contact.setContactPhone(contactRequest.getContactPhone());
            contact.setRelationship(contactRequest.getRelationship());
            contact.setIsDefault(YesNo.valueOf(contactRequest.getIsDefault()));

            if (contactRequest.getEmail() != null) {
                contact.setEmail(contactRequest.getEmail());
            }
            if (contactRequest.getAddressLine1() != null) {
                contact.setAddressLine1(contactRequest.getAddressLine1());
            }
            if (contactRequest.getAddressLine2() != null) {
                contact.setAddressLine2(contactRequest.getAddressLine2());
            }
            if (contactRequest.getAddressLine3() != null) {
                contact.setAddressLine3(contactRequest.getAddressLine3());
            }
            if (contactRequest.getAddressLine4() != null) {
                contact.setAddressLine4(contactRequest.getAddressLine4());
            }
            if (contactRequest.getNotes() != null) {
                contact.setNotes(contactRequest.getNotes());
            }

            // Set audit fields
            if(contact.getId() == null) {
                contact.setCreatedUser(LoginAuthentication.getUserName());
                contact.setCreatedDate(DateUtil.getDate());
            } else {
                contact.setModifiedUser(LoginAuthentication.getUserName());
                contact.setModifiedDate(DateUtil.getDate());
            }

            // Save the emergency contact
            emergencyContactRepository.save(contact);
            log.info("Emergency contact saved for user ID: {}", user.getId());
        }
    }

}

