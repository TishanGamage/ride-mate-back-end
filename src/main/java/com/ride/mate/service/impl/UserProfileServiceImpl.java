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
import com.ride.mate.resources.ProfilePhotoUpdateResource;
import com.ride.mate.resources.UserProfileAddResource;
import com.ride.mate.resources.UserProfileResponse;
import com.ride.mate.resources.UserProfileUpdateResource;
import com.ride.mate.resources.WillingToDriveUpdateResource;
import com.ride.mate.resources.UpdateRoleRequest;
import com.ride.mate.resources.SuccessAndErrorDetailsResource;
import com.ride.mate.enums.UserRole;
import com.ride.mate.service.DriverProfileService;
import com.ride.mate.service.UserProfileService;
import com.ride.mate.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


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
 * 5 16-03-2026    N/A          N/A          Tishan          Changed getUserProfileByUserId to return UserProfileResponse
 * 6 18-03-2026    N/A          N/A          Tishan          Added updateWillingToDrive method
 * 7 19-03-2026    N/A          N/A          Tishan          Added updateProfilePhoto method
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
    private final DriverProfileService driverProfileService;
    private final Environment environment;

    public UserProfileServiceImpl(UserProfileRepository userProfileRepository,
                                  UserRepository userRepository,
                                  DocumentDetailsRepository documentDetailsRepository,
                                  UserIdentificationDetailsRepository userIdentificationDetailsRepository,
                                  IdentificationTypeRepository identificationTypeRepository,
                                  EmergencyContactRepository emergencyContactRepository,
                                  DriverProfileService driverProfileService,
                                  Environment environment) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.documentDetailsRepository = documentDetailsRepository;
        this.userIdentificationDetailsRepository = userIdentificationDetailsRepository;
        this.identificationTypeRepository = identificationTypeRepository;
        this.emergencyContactRepository = emergencyContactRepository;
        this.driverProfileService = driverProfileService;
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

        // Set optional user verification image document
        if (request.getUserVerificationImageDocumentId() != null) {
            documentDetailsRepository.findById(request.getUserVerificationImageDocumentId())
                    .ifPresent(userProfile::setUserVerificationImageDocument);
        }

        // Map fields from request
        if (request.getDateOfBirth() != null) {
            userProfile.setDateOfBirth(DateUtil.stringToLocalDate(request.getDateOfBirth()));
        }
        userProfile.setGender(request.getGender());
        userProfile.setPreferredLanguage("EN");
        userProfile.setUserProfileCompleted("NO");
        userProfile.setWillingToDrive(request.getWillingToDrive() != null ? request.getWillingToDrive() : YesNo.NO);

        // Set audit fields
        userProfile.setCreatedDate(DateUtil.getDate());
        userProfile.setCreatedUser(LoginAuthentication.getUserName());
        userProfile.setSyncTs(DateUtil.getDate());

        // Save to database
        UserProfile savedProfile = userProfileRepository.saveAndFlush(userProfile);
        log.info("User profile created successfully with ID: {} for user ID: {}", savedProfile.getId(), request.getUserId());

        // Handle user identification details if provided
        boolean hasIdentificationDetails = false;
        if (request.getUserIdentificationDetails() != null) {
            setUserIdentificationDetails(user, request.getUserIdentificationDetails());
            hasIdentificationDetails = true;
        }

        if (YesNo.YES.equals(request.getWillingToDrive())) {
            if (request.getDriverDetails() != null) {
                driverProfileService.saveDriverProfile(user.getId(), request.getDriverDetails());
            }
        }

        // Evaluate and update profile completion status
        evaluateProfileCompletion(userProfile, hasIdentificationDetails);

        return savedProfile;
    }

    @Override
    public UserProfileResponse getUserProfileByUserId(Long userId) {

        log.info("Fetching user profile for user ID: {}", userId);

        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("User profile not found for user ID: {}", userId);
                    return new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message");
                });

        User user = userProfile.getUser();

        // Fetch identification details
        List<UserIdentificationDetails> identificationList = userIdentificationDetailsRepository.findByUserId(userId);
        UserIdentificationDetails identification = identificationList.isEmpty() ? null : identificationList.get(0);

        return UserProfileResponse.builder()
                .id(userProfile.getId())
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getUserRole().name())
                .status(user.getStatus().name())
                .emailVerified(user.getEmailVerified().name())
                .dateOfBirth(userProfile.getDateOfBirth() != null ? userProfile.getDateOfBirth().toString() : null)
                .gender(userProfile.getGender())
                .bio(userProfile.getBio())
                .addressLine1(userProfile.getAddressLine1())
                .addressLine2(userProfile.getAddressLine2())
                .addressLine3(userProfile.getAddressLine3())
                .addressLine4(userProfile.getAddressLine4())
                .city(userProfile.getCity())
                .state(userProfile.getState())
                .postalCode(userProfile.getPostalCode())
                .country(userProfile.getCountry())
                .preferredLanguage(userProfile.getPreferredLanguage())
                .userProfileCompleted(userProfile.getUserProfileCompleted())
                .willingToDrive(userProfile.getWillingToDrive() != null ? userProfile.getWillingToDrive().name() : null)
                .profileImageDocumentId(userProfile.getProfileImageDocument() != null ? userProfile.getProfileImageDocument().getId() : null)
                .profileImageUrl(userProfile.getProfileImageDocument() != null ? userProfile.getProfileImageDocument().getDocumentUrl() : null)
                .userVerificationImageDocumentId(userProfile.getUserVerificationImageDocument() != null ? userProfile.getUserVerificationImageDocument().getId() : null)
                .userVerificationImageUrl(userProfile.getUserVerificationImageDocument() != null ? userProfile.getUserVerificationImageDocument().getDocumentUrl() : null)
                .identificationTypeId(identification != null && identification.getIdentificationType() != null ? identification.getIdentificationType().getId() : null)
                .identificationTypeName(identification != null && identification.getIdentificationType() != null ? identification.getIdentificationType().getName() : null)
                .identificationNumber(identification != null ? identification.getIdentificationNumber() : null)
                .identificationFrontImageUrl(identification != null && identification.getFrontImageDocument() != null ? identification.getFrontImageDocument().getDocumentUrl() : null)
                .identificationBackImageUrl(identification != null && identification.getBackImageDocument() != null ? identification.getBackImageDocument().getDocumentUrl() : null)
                .createdDate(userProfile.getCreatedDate() != null ? userProfile.getCreatedDate().toString() : null)
                .modifiedDate(userProfile.getModifiedDate() != null ? userProfile.getModifiedDate().toString() : null)
                .build();
    }

    @Override
    public UserProfile updateUserProfile(UserProfileUpdateResource request, Long userId) {

        // Find existing profile by user ID
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("User profile update failed: Profile not found for user ID - {}", userId);
                    return new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message");
                });

        // Check optimistic locking version (skip if not provided)
        if (request.getVersion() != null && !userProfile.getVersion().equals(request.getVersion())) {
            throw new ValidateRecordException(environment.getProperty(RECORD_VERSION_MISMATCH), "errorMessage");
        }

        // Update optional profile image document
        if (request.getProfileImageDocumentId() != null) {
            documentDetailsRepository.findById(request.getProfileImageDocumentId())
                    .ifPresent(userProfile::setProfileImageDocument);
        }

        // Update optional user verification image document
        if (request.getUserVerificationImageDocumentId() != null) {
            documentDetailsRepository.findById(request.getUserVerificationImageDocumentId())
                    .ifPresent(userProfile::setUserVerificationImageDocument);
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
        if (request.getWillingToDrive() != null) {
            userProfile.setWillingToDrive(request.getWillingToDrive());
        }

        // Update audit fields
        userProfile.setModifiedDate(DateUtil.getDate());
        userProfile.setModifiedUser(LoginAuthentication.getUserName());
        userProfile.setSyncTs(DateUtil.getDate());

        // Save updated profile
        UserProfile updatedProfile = userProfileRepository.saveAndFlush(userProfile);
        log.info("User profile updated successfully with ID: {}", updatedProfile.getId());

        // Handle user identification details if provided
        boolean hasIdentificationDetails = userIdentificationDetailsRepository.existsByUserId(userProfile.getUser().getId());
        if (request.getUserIdentificationDetails() != null) {
            setUserIdentificationDetails(userProfile.getUser(), request.getUserIdentificationDetails());
            hasIdentificationDetails = true;
        }

        // Handle emergency contact details if provided
        if (request.getEmergencyContactDetails() != null) {
            setEmergencyContactDetails(userProfile.getUser(), request.getEmergencyContactDetails());
        }

        // Handle driver profile details if willing to drive
        if (YesNo.YES.equals(request.getWillingToDrive())) {
            if (request.getDriverDetails() != null) {
                driverProfileService.saveDriverProfile(userProfile.getUser().getId(), request.getDriverDetails());
            }
        }

        // Evaluate and update profile completion status
        // Use the in-memory userProfile object which has all associations set (updatedProfile may have lazy proxies)
        evaluateProfileCompletion(userProfile, hasIdentificationDetails);

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
        details.setSyncTs(DateUtil.getDate());

        // Save and flush the identification details so they are visible within the same transaction
        userIdentificationDetailsRepository.saveAndFlush(details);
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


    private void evaluateProfileCompletion(UserProfile userProfile, boolean hasIdentificationDetails) {

        boolean profileComplete =
                userProfile.getDateOfBirth() != null &&
                userProfile.getGender() != null &&
                userProfile.getUserVerificationImageDocument() != null &&
                hasIdentificationDetails;


        String completionStatus = profileComplete ? "YES" : "NO";

        // Only update if the status has changed
        if (!completionStatus.equals(userProfile.getUserProfileCompleted())) {
            userProfile.setUserProfileCompleted(completionStatus);
            userProfile.setModifiedDate(DateUtil.getDate());
            userProfile.setModifiedUser(LoginAuthentication.getUserName());
            userProfile.setSyncTs(DateUtil.getDate());
            userProfileRepository.save(userProfile);
            log.info("User profile completion status updated to '{}' for profile ID: {}", completionStatus, userProfile.getId());
        }
    }

    @Override
    public UserProfile updateWillingToDrive(Long id, WillingToDriveUpdateResource request) {

        log.info("Processing willingToDrive update for user profile ID: {}", id);
        // Fetch existing user profile
        UserProfile userProfile = userProfileRepository.findByUserId(id)
                .orElseThrow(() -> {
                    log.warn("User profile not found for ID: {}", id);
                    return new ValidateRecordException(
                            environment.getProperty(RECORD_NOT_FOUND), "message");
                });

        // Validate mandatory audit fields are present on the existing record
        if (userProfile.getCreatedDate() == null || userProfile.getCreatedUser() == null || userProfile.getSyncTs() == null) {
            log.warn("User profile ID {} is missing mandatory audit fields (createdDate/createdUser/syncTs)", id);
            throw new ValidateRecordException(
                    environment.getProperty(RECORD_NOT_FOUND), "message");
        }

        // Update only the willingToDrive field
        userProfile.setWillingToDrive(request.getWillingToDrive());
        userProfile.setModifiedDate(DateUtil.getDate());
        userProfile.setModifiedUser(LoginAuthentication.getUserName());
        userProfile.setSyncTs(DateUtil.getDate());

        UserProfile updatedProfile = userProfileRepository.saveAndFlush(userProfile);
        log.info("willingToDrive updated successfully for user profile ID: {} to value: {}",
                updatedProfile.getId(), updatedProfile.getWillingToDrive());

        return updatedProfile;
    }

    @Override
    public UserProfile updateProfilePhoto(Long userId, ProfilePhotoUpdateResource request) {

        log.info("Processing profile photo update for user ID: {}", userId);

        // Fetch existing user profile by userId
        UserProfile userProfile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Profile photo update failed: User profile not found for user ID - {}", userId);
                    return new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message");
                });

        // Validate that the document exists
        documentDetailsRepository.findById(request.getProfileImageDocumentId())
                .ifPresentOrElse(
                        userProfile::setProfileImageDocument,
                        () -> {
                            log.warn("Profile photo update failed: Document not found with ID - {}", request.getProfileImageDocumentId());
                            throw new ValidateRecordException(environment.getProperty(PROFILE_PHOTO_NOT_FOUND), "message");
                        }
                );
        // Update audit fields
        userProfile.setModifiedDate(DateUtil.getDate());
        userProfile.setModifiedUser(LoginAuthentication.getUserName());
        userProfile.setSyncTs(DateUtil.getDate());

        UserProfile updatedProfile = userProfileRepository.saveAndFlush(userProfile);
        log.info("Profile photo updated successfully for user ID: {} with document ID: {}", userId, request.getProfileImageDocumentId());
        return updatedProfile;
    }

    @Override
    public SuccessAndErrorDetailsResource updateRole(Long userId, UpdateRoleRequest request) {        log.info("Processing role update for user ID: {}", userId);

        UserRole newRole;
        try {
            newRole = UserRole.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Role update failed: Invalid role value - {}", request.getRole());
            throw new ValidateRecordException(environment.getProperty(ROLE_INVALID), "errorMessage");
        }

        if (newRole != UserRole.DRIVER && newRole != UserRole.PASSENGER) {
            log.warn("Role update failed: Role not allowed - {}", newRole);
            throw new ValidateRecordException(environment.getProperty(ROLE_INVALID), "errorMessage");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidateRecordException(environment.getProperty(LOGIN_USER_NOT_FOUND), "errorMessage"));

        user.setUserRole(newRole);
        user.setModifiedDate(DateUtil.getDate());
        user.setModifiedUser(SYSTEM);
        userRepository.save(user);

        log.info("Role updated successfully to {} for user ID: {}", newRole, userId);
        return new SuccessAndErrorDetailsResource(environment.getProperty(ROLE_UPDATE_SUCCESS));
    }
}