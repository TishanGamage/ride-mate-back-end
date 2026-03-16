package com.ride.mate.service.impl;

import com.ride.mate.core.LoginAuthentication;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.DriverProfile;
import com.ride.mate.domain.DriverVehicleDetails;
import com.ride.mate.domain.EmergencyContact;
import com.ride.mate.domain.User;
import com.ride.mate.domain.UserIdentificationDetails;
import com.ride.mate.domain.UserProfile;
import com.ride.mate.enums.YesNo;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.*;
import com.ride.mate.resources.DriverProfileRequestResource;
import com.ride.mate.resources.DriverVehicleDetailsRequestResource;
import com.ride.mate.resources.UserEmergencyContactDetailsRequestResource;
import com.ride.mate.resources.UserIdentificationDetailsRequestResource;
import com.ride.mate.resources.UserProfileAddResource;
import com.ride.mate.resources.UserProfileResponse;
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
 * 5 16-03-2026    N/A          N/A          Tishan          Changed getUserProfileByUserId to return UserProfileResponse
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
    private final DriverProfileRepository driverProfileRepository;
    private final DriverVehicleDetailsRepository driverVehicleDetailsRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final VehicleMakeRepository vehicleMakeRepository;
    private final Environment environment;

    public UserProfileServiceImpl(UserProfileRepository userProfileRepository,
                                  UserRepository userRepository,
                                  DocumentDetailsRepository documentDetailsRepository,
                                  UserIdentificationDetailsRepository userIdentificationDetailsRepository,
                                  IdentificationTypeRepository identificationTypeRepository,
                                  EmergencyContactRepository emergencyContactRepository,
                                  DriverProfileRepository driverProfileRepository,
                                  DriverVehicleDetailsRepository driverVehicleDetailsRepository,
                                  VehicleTypeRepository vehicleTypeRepository,
                                  VehicleMakeRepository vehicleMakeRepository,
                                  Environment environment) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.documentDetailsRepository = documentDetailsRepository;
        this.userIdentificationDetailsRepository = userIdentificationDetailsRepository;
        this.identificationTypeRepository = identificationTypeRepository;
        this.emergencyContactRepository = emergencyContactRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.driverVehicleDetailsRepository = driverVehicleDetailsRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.vehicleMakeRepository = vehicleMakeRepository;
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

        if(request.getWillingToDrive().equalsIgnoreCase(YesNo.YES.toString())) {
            if (request.getDriverDetails() != null) {
                setDriverProfileDetails(user, request.getDriverDetails());
            }
        }

        // Evaluate and update profile completion status
        evaluateProfileCompletion(savedProfile);

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
                .profileImageDocumentId(userProfile.getProfileImageDocument() != null ? userProfile.getProfileImageDocument().getId() : null)
                .profileImageUrl(userProfile.getProfileImageDocument() != null ? userProfile.getProfileImageDocument().getDocumentUrl() : null)
                .userVerificationImageDocumentId(userProfile.getUserVerificationImageDocument() != null ? userProfile.getUserVerificationImageDocument().getId() : null)
                .userVerificationImageUrl(userProfile.getUserVerificationImageDocument() != null ? userProfile.getUserVerificationImageDocument().getDocumentUrl() : null)
                .createdDate(userProfile.getCreatedDate() != null ? userProfile.getCreatedDate().toString() : null)
                .modifiedDate(userProfile.getModifiedDate() != null ? userProfile.getModifiedDate().toString() : null)
                .build();
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

        // Handle driver profile details if willing to drive
        if (request.getWillingToDrive() != null &&
                request.getWillingToDrive().equalsIgnoreCase(YesNo.YES.toString())) {
            if (request.getDriverDetails() != null) {
                setDriverProfileDetails(userProfile.getUser(), request.getDriverDetails());
            }
        }

        // Evaluate and update profile completion status
        evaluateProfileCompletion(updatedProfile);

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

    private void setDriverProfileDetails(User user, DriverProfileRequestResource driverRequest) {

        // Find existing driver profile or create a new one
        DriverProfile driverProfile = driverProfileRepository.findByUserId(user.getId())
                .orElse(new DriverProfile());

        boolean isNew = driverProfile.getId() == null;

        // Validate uniqueness of license number for new profile
        if (isNew && driverProfileRepository.existsByDriverLicenseNumber(driverRequest.getDriverLicenseNumber())) {
            log.warn("Driver profile creation failed: License number already exists - {}", driverRequest.getDriverLicenseNumber());
            throw new ValidateRecordException(environment.getProperty(DRIVER_PROFILE_ALREADY_EXISTS), "errorMessage");
        }

        // Set User relationship
        driverProfile.setUser(user);

        // Map fields from request
        driverProfile.setDriverLicenseNumber(driverRequest.getDriverLicenseNumber());
        driverProfile.setDriverLicenseExpiry(DateUtil.stringToLocalDate(driverRequest.getDriverLicenseExpiry()));
        driverProfile.setDriverLicenseVerified(YesNo.NO);

        // Set license document references if provided
        if (driverRequest.getDriverLicenseFrontDocumentId() != null) {
            documentDetailsRepository.findById(driverRequest.getDriverLicenseFrontDocumentId())
                    .ifPresent(driverProfile::setDriverLicenseFrontDocument);
        }
        if (driverRequest.getDriverLicenseBackDocumentId() != null) {
            documentDetailsRepository.findById(driverRequest.getDriverLicenseBackDocumentId())
                    .ifPresent(driverProfile::setDriverLicenseBackDocument);
        }

        // Set default fields for new profiles
        if (isNew) {
            driverProfile.setAccountStatus("PENDING");
            driverProfile.setApprovedBy(SYSTEM);
            driverProfile.setCreatedUser(LoginAuthentication.getUserName());
            driverProfile.setCreatedDate(DateUtil.getDate());
        } else {
            driverProfile.setModifiedUser(LoginAuthentication.getUserName());
            driverProfile.setModifiedDate(DateUtil.getDate());
        }
        driverProfile.setSyncTs(DateUtil.getDate());

        // Save the driver profile
        DriverProfile savedDriverProfile = driverProfileRepository.save(driverProfile);
        log.info("Driver profile saved for user ID: {}", user.getId());

        // Handle vehicle details if provided
        if (driverRequest.getVehicleDetails() != null) {
            setDriverVehicleDetails(savedDriverProfile, driverRequest.getVehicleDetails());
        }
    }

    private void setDriverVehicleDetails(DriverProfile driverProfile, DriverVehicleDetailsRequestResource vehicleRequest) {

        // Create new vehicle details entry
        DriverVehicleDetails vehicle = new DriverVehicleDetails();

        // Set DriverProfile relationship
        vehicle.setDriverProfile(driverProfile);

        // Resolve and set VehicleType
        vehicleTypeRepository.findById(vehicleRequest.getVehicleTypeId())
                .ifPresentOrElse(vehicle::setVehicleType, () -> {
                    throw new ValidateRecordException(environment.getProperty(VEHICLE_TYPE_NOT_FOUND), "errorMessage");
                });

        // Resolve and set VehicleMake
        vehicleMakeRepository.findById(vehicleRequest.getVehicleMakeId())
                .ifPresentOrElse(vehicle::setVehicleMake, () -> {
                    throw new ValidateRecordException(environment.getProperty(VEHICLE_MAKE_NOT_FOUND), "errorMessage");
                });

        // Map all fields from request
        vehicle.setRegistrationNumber(vehicleRequest.getRegistrationNumber());
        vehicle.setModel(vehicleRequest.getModel());
        vehicle.setYear(vehicleRequest.getYear());
        vehicle.setColor(vehicleRequest.getColor());
        vehicle.setSeats(vehicleRequest.getSeats());

        // Set optional document references
        if (vehicleRequest.getVehicleImageDocumentId() != null) {
            documentDetailsRepository.findById(vehicleRequest.getVehicleImageDocumentId())
                    .ifPresent(vehicle::setVehicleImageDocument);
        }
        if (vehicleRequest.getRegistrationCertificateDocumentId() != null) {
            documentDetailsRepository.findById(vehicleRequest.getRegistrationCertificateDocumentId())
                    .ifPresent(vehicle::setRegistrationCertificateDocument);
        }
        if (vehicleRequest.getInsuranceDocumentId() != null) {
            documentDetailsRepository.findById(vehicleRequest.getInsuranceDocumentId())
                    .ifPresent(vehicle::setInsuranceDocument);
        }

        // Set optional insurance details
        vehicle.setInsuranceNumber(vehicleRequest.getInsuranceNumber());
        vehicle.setInsuranceProvider(vehicleRequest.getInsuranceProvider());
        if (vehicleRequest.getInsuranceExpiry() != null) {
            vehicle.setInsuranceExpiry(DateUtil.stringToLocalDate(vehicleRequest.getInsuranceExpiry()));
        }

        // Set default status fields
        vehicle.setIsVerified(YesNo.NO);
        vehicle.setIsPrimary(YesNo.YES);
        vehicle.setIsActive(YesNo.YES);
        vehicle.setStatus("PENDING");

        // Set audit fields
        vehicle.setCreatedUser(LoginAuthentication.getUserName());
        vehicle.setCreatedDate(DateUtil.getDate());
        vehicle.setSyncTs(DateUtil.getDate());

        // Save the vehicle details
        driverVehicleDetailsRepository.save(vehicle);
        log.info("Driver vehicle details saved for driver profile ID: {}", driverProfile.getId());
    }

    private void evaluateProfileCompletion(UserProfile userProfile) {

        boolean profileComplete =
                userProfile.getDateOfBirth() != null &&
                userProfile.getGender() != null &&
                userProfile.getProfileImageDocument() != null &&
                userProfile.getUserVerificationImageDocument() != null &&
                userIdentificationDetailsRepository.existsByUserId(userProfile.getUser().getId());

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
}