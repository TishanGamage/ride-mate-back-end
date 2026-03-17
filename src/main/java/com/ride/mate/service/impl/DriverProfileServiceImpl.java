package com.ride.mate.service.impl;

import com.ride.mate.core.LoginAuthentication;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.DriverProfile;
import com.ride.mate.domain.DriverVehicleDetails;
import com.ride.mate.domain.User;
import com.ride.mate.enums.YesNo;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.DocumentDetailsRepository;
import com.ride.mate.repository.DriverProfileRepository;
import com.ride.mate.repository.DriverVehicleDetailsRepository;
import com.ride.mate.repository.UserRepository;
import com.ride.mate.repository.VehicleMakeRepository;
import com.ride.mate.repository.VehicleTypeRepository;
import com.ride.mate.resources.DriverProfileRequestResource;
import com.ride.mate.resources.DriverVehicleDetailsRequestResource;
import com.ride.mate.service.DriverProfileService;
import com.ride.mate.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DriverProfileServiceImpl
 * Implementation of driver profile management business logic
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 16-03-2026    N/A          N/A          Tishan          Initial Development
 * 2 17-03-2026    N/A          N/A          Tishan          Added driverProfileCompleted evaluation
 */
@Slf4j
@Service
@Transactional
public class DriverProfileServiceImpl extends MessagePropertyBase implements DriverProfileService {

    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final DriverVehicleDetailsRepository driverVehicleDetailsRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final VehicleMakeRepository vehicleMakeRepository;
    private final DocumentDetailsRepository documentDetailsRepository;
    private final Environment environment;

    public DriverProfileServiceImpl(UserRepository userRepository,
                                    DriverProfileRepository driverProfileRepository,
                                    DriverVehicleDetailsRepository driverVehicleDetailsRepository,
                                    VehicleTypeRepository vehicleTypeRepository,
                                    VehicleMakeRepository vehicleMakeRepository,
                                    DocumentDetailsRepository documentDetailsRepository,
                                    Environment environment) {
        this.userRepository = userRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.driverVehicleDetailsRepository = driverVehicleDetailsRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.vehicleMakeRepository = vehicleMakeRepository;
        this.documentDetailsRepository = documentDetailsRepository;
        this.environment = environment;
    }

    @Override
    public DriverProfile saveDriverProfile(Long userId, DriverProfileRequestResource request) {

        log.info("Processing driver profile save for user ID: {}", userId);

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Driver profile save failed: User not found - {}", userId);
                    return new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message");
                });

        // Find existing driver profile or create a new one
        DriverProfile driverProfile = driverProfileRepository.findByUserId(userId)
                .orElse(new DriverProfile());

        boolean isNew = driverProfile.getId() == null;

        // Validate uniqueness of license number for new profile
        if (isNew && driverProfileRepository.existsByDriverLicenseNumber(request.getDriverLicenseNumber())) {
            log.warn("Driver profile creation failed: License number already exists - {}", request.getDriverLicenseNumber());
            throw new ValidateRecordException(environment.getProperty(DRIVER_PROFILE_ALREADY_EXISTS), "errorMessage");
        }

        // Set User relationship
        driverProfile.setUser(user);

        // Map fields from request
        driverProfile.setDriverLicenseNumber(request.getDriverLicenseNumber());
        driverProfile.setDriverLicenseExpiry(DateUtil.stringToLocalDate(request.getDriverLicenseExpiry()));
        driverProfile.setDriverLicenseVerified(YesNo.NO);

        // Set license document references if provided
        if (request.getDriverLicenseFrontDocumentId() != null) {
            documentDetailsRepository.findById(request.getDriverLicenseFrontDocumentId())
                    .ifPresent(driverProfile::setDriverLicenseFrontDocument);
        }
        if (request.getDriverLicenseBackDocumentId() != null) {
            documentDetailsRepository.findById(request.getDriverLicenseBackDocumentId())
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
        DriverProfile savedDriverProfile = driverProfileRepository.saveAndFlush(driverProfile);
        log.info("Driver profile saved successfully with ID: {} for user ID: {}", savedDriverProfile.getId(), userId);

        // Handle vehicle details if provided
        boolean hasVehicleDetails = false;
        if (request.getVehicleDetails() != null) {
            saveDriverVehicleDetails(savedDriverProfile, request.getVehicleDetails());
            hasVehicleDetails = true;
        }

        // Evaluate and update driver profile completion status
        evaluateDriverProfileCompletion(driverProfile, hasVehicleDetails);

        return savedDriverProfile;
    }

    private void saveDriverVehicleDetails(DriverProfile driverProfile, DriverVehicleDetailsRequestResource vehicleRequest) {

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

    private void evaluateDriverProfileCompletion(DriverProfile driverProfile, boolean hasVehicleDetails) {

        boolean profileComplete =
                driverProfile.getDriverLicenseNumber() != null &&
                driverProfile.getDriverLicenseExpiry() != null &&
                driverProfile.getDriverLicenseFrontDocument() != null &&
                driverProfile.getDriverLicenseBackDocument() != null &&
                hasVehicleDetails;

        String completionStatus = profileComplete ? "YES" : "NO";

        // Only update if the status has changed
        if (!completionStatus.equals(driverProfile.getDriverProfileCompleted())) {
            driverProfile.setDriverProfileCompleted(completionStatus);
            driverProfile.setModifiedDate(DateUtil.getDate());
            driverProfile.setModifiedUser(LoginAuthentication.getUserName());
            driverProfile.setSyncTs(DateUtil.getDate());
            driverProfileRepository.save(driverProfile);
            log.info("Driver profile completion status updated to '{}' for driver profile ID: {}", completionStatus, driverProfile.getId());
        }
    }
}

