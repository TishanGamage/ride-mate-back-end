package com.ride.mate.service.impl;

import com.ride.mate.core.LoginAuthentication;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.EmergencyContact;
import com.ride.mate.domain.User;
import com.ride.mate.domain.UserIdentificationDetails;
import com.ride.mate.domain.UserProfile;
import com.ride.mate.enums.YesNo;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.*;
import com.ride.mate.resources.UserEmergencyContactDetailsRequestResource;
import com.ride.mate.resources.UserIdentificationDetailsRequestResource;
import com.ride.mate.resources.UserProfileAddResource;
import com.ride.mate.resources.UserProfileUpdateResource;
import com.ride.mate.service.UserProfileService;
import com.ride.mate.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserProfileServiceImpl
 * Implementation of user profile management business logic
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 15-03-2026    N/A          N/A          Tishan          Initial Development
 * 2 15-03-2026    N/A          N/A          Tishan          Added identification and emergency contact handling
 * 3 15-03-2026    N/A          N/A          Tishan          Added updateUserProfile method
 * 4 15-03-2026    N/A          N/A          Tishan          Added getUserProfileByUserId method
 */
@Slf4j
@Service
@Transactional
public class UserProfileServiceImpl extends MessagePropertyBase implements UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final DocumentDetailsRepository documentDetailsRepository;
    private final UserIdentificationDetailsRepository userIdentificationDetailsRepository;
    private final IdentificationTypeRepository identificationTypeRepository;
    private final EmergencyContactRepository emergencyContactRepository;
    private final Environment environment;

    public UserProfileServiceImpl(UserProfileRepository userProfileRepository,
                                  UserRepository userRepository,
                                  DocumentDetailsRepository documentDetailsRepository,
                                  UserIdentificationDetailsRepository userIdentificationDetailsRepository,
                                  IdentificationTypeRepository identificationTypeRepository,
                                  EmergencyContactRepository emergencyContactRepository,
                                  Environment environment) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.documentDetailsRepository = documentDetailsRepository;
        this.userIdentificationDetailsRepository = userIdentificationDetailsRepository;
        this.identificationTypeRepository = identificationTypeRepository;
        this.emergencyContactRepository = emergencyContactRepository;
        this.environment = environment;
    }

    @Override
    public UserProfile createUserProfile(UserProfileAddResource request) {

        log.info("Processing user profile creation for user ID: {}", request.getUserId());

        // Validate user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> {
                    log.warn("User profile creation failed: User not found - {}", request.getUserId());
                    return new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message");
                });

        // Validate profile does not already exist for user
        if (userProfileRepository.existsByUserId(request.getUserId())) {
            log.warn("User profile creation failed: Profile already exists for user ID - {}", request.getUserId());
            throw new ValidateRecordException(environment.getProperty(USER_PROFILE_ALREADY_EXISTS), "errorMessage");
        }

        // Create new user profile entity
        UserProfile userProfile = new UserProfile();
        userProfile.setUser(user);

        // Set optional profile image document
        if (request.getProfileImageDocumentId() != null) {
            documentDetailsRepository.findById(request.getProfileImageDocumentId())
                    .ifPresent(userProfile::setProfileImageDocument);
        }

        // Map fields from request
        if (request.getDateOfBirth() != null) {
            userProfile.setDateOfBirth(DateUtil.stringToLocalDate(request.getDateOfBirth()));
        }
        userProfile.setGender(request.getGender());
        userProfile.setPreferredLanguage("EN");
        userProfile.setUserProfileCompleted("NO");

        // Set audit fields
        userProfile.setCreatedDate(DateUtil.getDate());
        userProfile.setCreatedUser(LoginAuthentication.getUserName());
        userProfile.setSyncTs(DateUtil.getDate());

        // Save to database
        UserProfile savedProfile = userProfileRepository.save(userProfile);
        log.info("User profile created successfully with ID: {} for user ID: {}", savedProfile.getId(), request.getUserId());

        // Handle user identification details if provided
        if (request.getUserIdentificationDetails() != null) {
            setUserIdentificationDetails(user, request.getUserIdentificationDetails());
        }

        return savedProfile;
    }

    @Override
    public UserProfile getUserProfileByUserId(Long userId) {

        log.info("Fetching user profile for user ID: {}", userId);

        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("User profile not found for user ID: {}", userId);
                    return new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message");
                });
    }

    @Override
    public UserProfile updateUserProfile(UserProfileUpdateResource request,Long id) {

        // Find existing profile
        UserProfile userProfile = userProfileRepository.findById(id)
                .orElseThrow(() -> {
                    return new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message");
                });

        // Check optimistic locking version
        if (!userProfile.getVersion().equals(request.getVersion())) {
            throw new ValidateRecordException(environment.getProperty(RECORD_VERSION_MISMATCH), "errorMessage");
        }

        // Update optional profile image document
        if (request.getProfileImageDocumentId() != null) {
            documentDetailsRepository.findById(request.getProfileImageDocumentId())
                    .ifPresent(userProfile::setProfileImageDocument);
        }

        // Update fields from request
        if (request.getDateOfBirth() != null) {
            userProfile.setDateOfBirth(DateUtil.stringToLocalDate(request.getDateOfBirth()));
        }
        if (request.getGender() != null) {
            userProfile.setGender(request.getGender());
        }
        if (request.getBio() != null) {
            userProfile.setBio(request.getBio());
        }
        if (request.getAddressLine1() != null) {
            userProfile.setAddressLine1(request.getAddressLine1());
        }
        if (request.getAddressLine2() != null) {
            userProfile.setAddressLine2(request.getAddressLine2());
        }
        if (request.getAddressLine3() != null) {
            userProfile.setAddressLine3(request.getAddressLine3());
        }
        if (request.getAddressLine4() != null) {
            userProfile.setAddressLine4(request.getAddressLine4());
        }
        if (request.getCity() != null) {
            userProfile.setCity(request.getCity());
        }
        if (request.getState() != null) {
            userProfile.setState(request.getState());
        }
        if (request.getPostalCode() != null) {
            userProfile.setPostalCode(request.getPostalCode());
        }
        if (request.getCountry() != null) {
            userProfile.setCountry(request.getCountry());
        }
        if (request.getPreferredLanguage() != null) {
            userProfile.setPreferredLanguage(request.getPreferredLanguage());
        }

        // Update audit fields
        userProfile.setModifiedDate(DateUtil.getDate());
        userProfile.setModifiedUser(LoginAuthentication.getUserName());
        userProfile.setSyncTs(DateUtil.getDate());

        // Save updated profile
        UserProfile updatedProfile = userProfileRepository.save(userProfile);
        log.info("User profile updated successfully with ID: {}", updatedProfile.getId());

        // Handle user identification details if provided
        if (request.getUserIdentificationDetails() != null) {
            setUserIdentificationDetails(userProfile.getUser(), request.getUserIdentificationDetails());
        }

        // Handle emergency contact details if provided
        if (request.getEmergencyContactDetails() != null) {
            setEmergencyContactDetails(userProfile.getUser(), request.getEmergencyContactDetails());
        }

        return updatedProfile;
    }

    private void setUserIdentificationDetails(User user, UserIdentificationDetailsRequestResource detailsRequest) {

        // Find or create UserIdentificationDetails
        UserIdentificationDetails details = userIdentificationDetailsRepository
                .findByUserIdAndIdentificationTypeId(user.getId(), detailsRequest.getIdentificationTypeId())
                .orElse(new UserIdentificationDetails());

        // Set User relationship
        details.setUser(user);

        // Set IdentificationType relationship
        identificationTypeRepository.findById(detailsRequest.getIdentificationTypeId())
                .ifPresentOrElse(details::setIdentificationType, () -> {
                    throw new ValidateRecordException(environment.getProperty(IDENTIFICATION_TYPE_NOT_FOUND), "errorMessage");
                });

        // Map all fields from request resource to domain entity
        details.setIdentificationNumber(detailsRequest.getIdentificationNumber());

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
        if (details.getId() == null) {
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

    private void setEmergencyContactDetails(User user, UserEmergencyContactDetailsRequestResource contactRequest) {

        // Find existing emergency contact for this user or create new one
        EmergencyContact contact = emergencyContactRepository
                .findByUserIdAndIsDefault(user.getId(), YesNo.YES)
                .orElse(new EmergencyContact());

        // Set User relationship
        contact.setUser(user);

        // Map all fields from request resource
        contact.setContactName(contactRequest.getContactName());
        contact.setContactPhone(contactRequest.getContactPhone());
        contact.setRelationship(contactRequest.getRelationship());

        // Set audit fields
        if (contact.getId() == null) {
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