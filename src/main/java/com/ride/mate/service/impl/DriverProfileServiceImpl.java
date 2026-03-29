package com.ride.mate.service.impl;

import com.ride.mate.core.LoginAuthentication;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.DriverProfile;
import com.ride.mate.domain.DriverVehicleDetails;
import com.ride.mate.domain.User;
import com.ride.mate.enums.DriverStatus;
import com.ride.mate.enums.YesNo;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.DocumentDetailsRepository;
import com.ride.mate.repository.DriverProfileRepository;
import com.ride.mate.repository.DriverVehicleDetailsRepository;
import com.ride.mate.repository.UserRepository;
import com.ride.mate.repository.VehicleMakeRepository;
import com.ride.mate.repository.VehicleModelRepository;
import com.ride.mate.repository.VehicleTypeRepository;
import com.ride.mate.resources.DriverProfileRequestResource;
import com.ride.mate.resources.DriverProfileResponse;
import com.ride.mate.resources.DriverVehicleDetailsResponse;
import com.ride.mate.resources.DriverVehicleDetailsRequestResource;
import com.ride.mate.resources.DriverVehicleListResponse;
import com.ride.mate.service.DriverProfileService;
import com.ride.mate.service.DriverWalletService;
import com.ride.mate.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
 * 3 18-03-2026    N/A          N/A          Tishan          Added vehicleModel, multiple vehicle image/insurance/revenue license docs
 * 4 20-03-2026    N/A          N/A          Danushka          Added wallet initialization on new driver profile creation
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
    private final VehicleModelRepository vehicleModelRepository;
    private final DocumentDetailsRepository documentDetailsRepository;
    private final DriverWalletService driverWalletService;
    private final Environment environment;

    public DriverProfileServiceImpl(UserRepository userRepository,
                                    DriverProfileRepository driverProfileRepository,
                                    DriverVehicleDetailsRepository driverVehicleDetailsRepository,
                                    VehicleTypeRepository vehicleTypeRepository,
                                    VehicleMakeRepository vehicleMakeRepository,
                                    VehicleModelRepository vehicleModelRepository,
                                    DocumentDetailsRepository documentDetailsRepository,
                                    DriverWalletService driverWalletService,
                                    Environment environment) {
        this.userRepository = userRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.driverVehicleDetailsRepository = driverVehicleDetailsRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.vehicleMakeRepository = vehicleMakeRepository;
        this.vehicleModelRepository = vehicleModelRepository;
        this.documentDetailsRepository = documentDetailsRepository;
        this.driverWalletService = driverWalletService;
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
            driverProfile.setAccountStatus(DriverStatus.PENDING);
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

        // Initialize wallet for new driver profiles
        if (isNew) {
            driverWalletService.initializeWallet(savedDriverProfile);
            log.info("Wallet initialized for new driver profile ID: {}", savedDriverProfile.getId());
        }

        // Handle vehicle details if provided
        boolean hasVehicleDetails = false;
        if (request.getVehicleDetails() != null) {
            saveDriverVehicleDetails(savedDriverProfile, request.getVehicleDetails(), true);
            hasVehicleDetails = true;
        }

        // Evaluate and update driver profile completion status
        evaluateDriverProfileCompletion(driverProfile, hasVehicleDetails);

        return savedDriverProfile;
    }

    @Override
    public DriverProfileResponse getDriverProfileByUserId(Long userId) {

        log.info("Fetching driver profile for user ID: {}", userId);

        DriverProfile driverProfile = driverProfileRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("Driver profile not found for user ID: {}", userId);
                    return new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message");
                });

        User user = driverProfile.getUser();

        // Map all vehicles associated with this driver profile
        List<DriverVehicleDetailsResponse> vehicleResponses = driverVehicleDetailsRepository
                .findByDriverProfileId(driverProfile.getId())
                .stream()
                .map(this::mapVehicleToResponse)
                .collect(Collectors.toList());

        return DriverProfileResponse.builder()
                .id(driverProfile.getId())
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .driverLicenseNumber(driverProfile.getDriverLicenseNumber())
                .driverLicenseExpiry(driverProfile.getDriverLicenseExpiry() != null ? driverProfile.getDriverLicenseExpiry().toString() : null)
                .driverLicenseVerified(driverProfile.getDriverLicenseVerified() != null ? driverProfile.getDriverLicenseVerified().name() : null)
                .driverLicenseFrontDocumentId(driverProfile.getDriverLicenseFrontDocument() != null ? driverProfile.getDriverLicenseFrontDocument().getId() : null)
                .driverLicenseFrontDocumentUrl(driverProfile.getDriverLicenseFrontDocument() != null ? driverProfile.getDriverLicenseFrontDocument().getDocumentUrl() : null)
                .driverLicenseBackDocumentId(driverProfile.getDriverLicenseBackDocument() != null ? driverProfile.getDriverLicenseBackDocument().getId() : null)
                .driverLicenseBackDocumentUrl(driverProfile.getDriverLicenseBackDocument() != null ? driverProfile.getDriverLicenseBackDocument().getDocumentUrl() : null)
                .ratingAsDriver(driverProfile.getRatingAsDriver() != null ? driverProfile.getRatingAsDriver().toPlainString() : null)
                .totalRidesAsDriver(driverProfile.getTotalRidesAsDriver())
                .totalEarnings(driverProfile.getTotalEarnings() != null ? driverProfile.getTotalEarnings().toPlainString() : null)
                .accountStatus(driverProfile.getAccountStatus().toString())
                .driverProfileCompleted(driverProfile.getDriverProfileCompleted())
                .vehicles(vehicleResponses)
                .createdDate(driverProfile.getCreatedDate() != null ? driverProfile.getCreatedDate().toString() : null)
                .modifiedDate(driverProfile.getModifiedDate() != null ? driverProfile.getModifiedDate().toString() : null)
                .build();
    }

    private DriverVehicleDetailsResponse mapVehicleToResponse(com.ride.mate.domain.DriverVehicleDetails v) {
        return DriverVehicleDetailsResponse.builder()
                .id(v.getId())
                .vehicleTypeId(v.getVehicleType() != null ? v.getVehicleType().getId() : null)
                .vehicleTypeName(v.getVehicleType() != null ? v.getVehicleType().getName() : null)
                .vehicleMakeId(v.getVehicleMake() != null ? v.getVehicleMake().getId() : null)
                .vehicleMakeName(v.getVehicleMake() != null ? v.getVehicleMake().getName() : null)
                .vehicleModelId(v.getVehicleModel() != null ? v.getVehicleModel().getId() : null)
                .vehicleModelName(v.getVehicleModel() != null ? v.getVehicleModel().getName() : null)
                .registrationNumber(v.getRegistrationNumber())
                .model(v.getModel())
                .year(v.getYear())
                .color(v.getColor())
                .seats(v.getSeats())
                .vehicleImageDocumentId1(v.getVehicleImageDocument1() != null ? v.getVehicleImageDocument1().getId() : null)
                .vehicleImageUrl1(v.getVehicleImageDocument1() != null ? v.getVehicleImageDocument1().getDocumentUrl() : null)
                .vehicleImageDocumentId2(v.getVehicleImageDocument2() != null ? v.getVehicleImageDocument2().getId() : null)
                .vehicleImageUrl2(v.getVehicleImageDocument2() != null ? v.getVehicleImageDocument2().getDocumentUrl() : null)
                .vehicleImageDocumentId3(v.getVehicleImageDocument3() != null ? v.getVehicleImageDocument3().getId() : null)
                .vehicleImageUrl3(v.getVehicleImageDocument3() != null ? v.getVehicleImageDocument3().getDocumentUrl() : null)
                .vehicleImageDocumentId4(v.getVehicleImageDocument4() != null ? v.getVehicleImageDocument4().getId() : null)
                .vehicleImageUrl4(v.getVehicleImageDocument4() != null ? v.getVehicleImageDocument4().getDocumentUrl() : null)
                .registrationCertificateDocumentId(v.getRegistrationCertificateDocument() != null ? v.getRegistrationCertificateDocument().getId() : null)
                .registrationCertificateUrl(v.getRegistrationCertificateDocument() != null ? v.getRegistrationCertificateDocument().getDocumentUrl() : null)
                .insuranceNumber(v.getInsuranceNumber())
                .insuranceProvider(v.getInsuranceProvider())
                .insuranceExpiry(v.getInsuranceExpiry() != null ? v.getInsuranceExpiry().toString() : null)
                .insuranceDocumentId1(v.getInsuranceDocument1() != null ? v.getInsuranceDocument1().getId() : null)
                .insuranceDocumentUrl1(v.getInsuranceDocument1() != null ? v.getInsuranceDocument1().getDocumentUrl() : null)
                .insuranceDocumentId2(v.getInsuranceDocument2() != null ? v.getInsuranceDocument2().getId() : null)
                .insuranceDocumentUrl2(v.getInsuranceDocument2() != null ? v.getInsuranceDocument2().getDocumentUrl() : null)
                .revenueLicenseDocumentId1(v.getRevenueLicenseDocument1() != null ? v.getRevenueLicenseDocument1().getId() : null)
                .revenueLicenseDocumentUrl1(v.getRevenueLicenseDocument1() != null ? v.getRevenueLicenseDocument1().getDocumentUrl() : null)
                .revenueLicenseDocumentId2(v.getRevenueLicenseDocument2() != null ? v.getRevenueLicenseDocument2().getId() : null)
                .revenueLicenseDocumentUrl2(v.getRevenueLicenseDocument2() != null ? v.getRevenueLicenseDocument2().getDocumentUrl() : null)
                .isVerified(v.getIsVerified() != null ? v.getIsVerified().name() : null)
                .isPrimary(v.getIsPrimary() != null ? v.getIsPrimary().name() : null)
                .status(v.getStatus())
                .createdDate(v.getCreatedDate() != null ? v.getCreatedDate().toString() : null)
                .modifiedDate(v.getModifiedDate() != null ? v.getModifiedDate().toString() : null)
                .build();
    }

    private DriverVehicleDetails saveDriverVehicleDetails(DriverProfile driverProfile, DriverVehicleDetailsRequestResource vehicleRequest, boolean isPrimary) {

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

        // Resolve and set VehicleModel (optional)
        if (vehicleRequest.getVehicleModelId() != null) {
            vehicleModelRepository.findById(vehicleRequest.getVehicleModelId())
                    .ifPresent(vehicle::setVehicleModel);
        }

        // Map all fields from request
        vehicle.setRegistrationNumber(vehicleRequest.getRegistrationNumber());
        vehicle.setModel(vehicleRequest.getModel());
        vehicle.setYear(vehicleRequest.getYear());
        vehicle.setColor(vehicleRequest.getColor());
        vehicle.setSeats(vehicleRequest.getSeats() != null ? vehicleRequest.getSeats() : 0);

        // Set optional vehicle image document references (up to 4)
        if (vehicleRequest.getVehicleImageDocumentId1() != null) {
            documentDetailsRepository.findById(vehicleRequest.getVehicleImageDocumentId1())
                    .ifPresent(vehicle::setVehicleImageDocument1);
        }
        if (vehicleRequest.getVehicleImageDocumentId2() != null) {
            documentDetailsRepository.findById(vehicleRequest.getVehicleImageDocumentId2())
                    .ifPresent(vehicle::setVehicleImageDocument2);
        }
        if (vehicleRequest.getVehicleImageDocumentId3() != null) {
            documentDetailsRepository.findById(vehicleRequest.getVehicleImageDocumentId3())
                    .ifPresent(vehicle::setVehicleImageDocument3);
        }
        if (vehicleRequest.getVehicleImageDocumentId4() != null) {
            documentDetailsRepository.findById(vehicleRequest.getVehicleImageDocumentId4())
                    .ifPresent(vehicle::setVehicleImageDocument4);
        }
        if (vehicleRequest.getRegistrationCertificateDocumentId() != null) {
            documentDetailsRepository.findById(vehicleRequest.getRegistrationCertificateDocumentId())
                    .ifPresent(vehicle::setRegistrationCertificateDocument);
        }
        if (vehicleRequest.getInsuranceDocumentId1() != null) {
            documentDetailsRepository.findById(vehicleRequest.getInsuranceDocumentId1())
                    .ifPresent(vehicle::setInsuranceDocument1);
        }
        if (vehicleRequest.getInsuranceDocumentId2() != null) {
            documentDetailsRepository.findById(vehicleRequest.getInsuranceDocumentId2())
                    .ifPresent(vehicle::setInsuranceDocument2);
        }
        if (vehicleRequest.getRevenueLicenseDocumentId1() != null) {
            documentDetailsRepository.findById(vehicleRequest.getRevenueLicenseDocumentId1())
                    .ifPresent(vehicle::setRevenueLicenseDocument1);
        }
        if (vehicleRequest.getRevenueLicenseDocumentId2() != null) {
            documentDetailsRepository.findById(vehicleRequest.getRevenueLicenseDocumentId2())
                    .ifPresent(vehicle::setRevenueLicenseDocument2);
        }

        // Set optional insurance details
        vehicle.setInsuranceNumber(vehicleRequest.getInsuranceNumber());
        vehicle.setInsuranceProvider(vehicleRequest.getInsuranceProvider());
        if (vehicleRequest.getInsuranceExpiry() != null) {
            vehicle.setInsuranceExpiry(DateUtil.stringToLocalDate(vehicleRequest.getInsuranceExpiry()));
        }

        // Set default status fields
        vehicle.setIsVerified(YesNo.NO);
        vehicle.setIsPrimary(isPrimary ? YesNo.YES : YesNo.NO);
        vehicle.setStatus("PENDING");

        // Set audit fields
        vehicle.setCreatedUser(LoginAuthentication.getUserName());
        vehicle.setCreatedDate(DateUtil.getDate());
        vehicle.setSyncTs(DateUtil.getDate());

        // Save the vehicle details
        DriverVehicleDetails saved = driverVehicleDetailsRepository.save(vehicle);
        log.info("Driver vehicle details saved for driver profile ID: {}", driverProfile.getId());
        return saved;
    }

    @Override
    public DriverVehicleDetails addVehicle(Long driverProfileId, DriverVehicleDetailsRequestResource request) {

        log.info("Processing add vehicle for driver profile ID: {}", driverProfileId);

        DriverProfile driverProfile = driverProfileRepository.findById(driverProfileId)
                .orElseThrow(() -> {
                    log.warn("Add vehicle failed: Driver profile not found - {}", driverProfileId);
                    return new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message");
                });

        if (driverVehicleDetailsRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            log.warn("Add vehicle failed: Registration number already exists - {}", request.getRegistrationNumber());
            throw new ValidateRecordException("Registration number already exists", "errorMessage");
        }

        // If driver already has a primary vehicle, new one is non-primary
        boolean hasPrimary = driverVehicleDetailsRepository
                .findFirstByDriverProfileIdAndIsPrimary(driverProfile.getId(), YesNo.YES).isPresent();

        return saveDriverVehicleDetails(driverProfile, request, !hasPrimary);
    }

    @Override
    public DriverVehicleDetails updateVehicle(Long vehicleId, DriverVehicleDetailsRequestResource request) {

        log.info("Processing update vehicle for vehicle ID: {}", vehicleId);

        DriverVehicleDetails vehicle = driverVehicleDetailsRepository.findById(vehicleId)
                .orElseThrow(() -> {
                    log.warn("Update vehicle failed: Vehicle not found - {}", vehicleId);
                    return new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message");
                });

        // Check registration number uniqueness only if it changed
        if (!vehicle.getRegistrationNumber().equals(request.getRegistrationNumber()) &&
                driverVehicleDetailsRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            log.warn("Update vehicle failed: Registration number already exists - {}", request.getRegistrationNumber());
            throw new ValidateRecordException("Registration number already exists", "errorMessage");
        }

        // Resolve and set VehicleType
        vehicleTypeRepository.findById(request.getVehicleTypeId())
                .ifPresentOrElse(vehicle::setVehicleType, () -> {
                    throw new ValidateRecordException(environment.getProperty(VEHICLE_TYPE_NOT_FOUND), "errorMessage");
                });

        // Resolve and set VehicleMake
        vehicleMakeRepository.findById(request.getVehicleMakeId())
                .ifPresentOrElse(vehicle::setVehicleMake, () -> {
                    throw new ValidateRecordException(environment.getProperty(VEHICLE_MAKE_NOT_FOUND), "errorMessage");
                });

        // Resolve and set VehicleModel (optional)
        if (request.getVehicleModelId() != null) {
            vehicleModelRepository.findById(request.getVehicleModelId()).ifPresent(vehicle::setVehicleModel);
        } else {
            vehicle.setVehicleModel(null);
        }

        vehicle.setRegistrationNumber(request.getRegistrationNumber());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setColor(request.getColor());
        vehicle.setSeats(request.getSeats() != null ? request.getSeats() : 0);

        // Update vehicle image documents
        vehicle.setVehicleImageDocument1(request.getVehicleImageDocumentId1() != null ?
                documentDetailsRepository.findById(request.getVehicleImageDocumentId1()).orElse(null) : null);
        vehicle.setVehicleImageDocument2(request.getVehicleImageDocumentId2() != null ?
                documentDetailsRepository.findById(request.getVehicleImageDocumentId2()).orElse(null) : null);
        vehicle.setVehicleImageDocument3(request.getVehicleImageDocumentId3() != null ?
                documentDetailsRepository.findById(request.getVehicleImageDocumentId3()).orElse(null) : null);
        vehicle.setVehicleImageDocument4(request.getVehicleImageDocumentId4() != null ?
                documentDetailsRepository.findById(request.getVehicleImageDocumentId4()).orElse(null) : null);

        vehicle.setRegistrationCertificateDocument(request.getRegistrationCertificateDocumentId() != null ?
                documentDetailsRepository.findById(request.getRegistrationCertificateDocumentId()).orElse(null) : null);

        vehicle.setInsuranceNumber(request.getInsuranceNumber());
        vehicle.setInsuranceProvider(request.getInsuranceProvider());
        vehicle.setInsuranceExpiry(request.getInsuranceExpiry() != null ? DateUtil.stringToLocalDate(request.getInsuranceExpiry()) : null);

        vehicle.setInsuranceDocument1(request.getInsuranceDocumentId1() != null ?
                documentDetailsRepository.findById(request.getInsuranceDocumentId1()).orElse(null) : null);
        vehicle.setInsuranceDocument2(request.getInsuranceDocumentId2() != null ?
                documentDetailsRepository.findById(request.getInsuranceDocumentId2()).orElse(null) : null);

        vehicle.setRevenueLicenseDocument1(request.getRevenueLicenseDocumentId1() != null ?
                documentDetailsRepository.findById(request.getRevenueLicenseDocumentId1()).orElse(null) : null);
        vehicle.setRevenueLicenseDocument2(request.getRevenueLicenseDocumentId2() != null ?
                documentDetailsRepository.findById(request.getRevenueLicenseDocumentId2()).orElse(null) : null);

        vehicle.setModifiedUser(LoginAuthentication.getUserName());
        vehicle.setModifiedDate(DateUtil.getDate());
        vehicle.setSyncTs(DateUtil.getDate());

        DriverVehicleDetails updated = driverVehicleDetailsRepository.save(vehicle);
        log.info("Vehicle updated successfully for vehicle ID: {}", vehicleId);
        return updated;
    }

    @Override
    public DriverVehicleListResponse getVehiclesByDriverProfileId(Long driverProfileId) {

        log.info("Fetching vehicles for driver profile ID: {}", driverProfileId);

        if (!driverProfileRepository.existsById(driverProfileId)) {
            log.warn("Get vehicles failed: Driver profile not found - {}", driverProfileId);
            throw new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message");
        }

        List<DriverVehicleDetailsResponse> vehicles = driverVehicleDetailsRepository
                .findByDriverProfileId(driverProfileId)
                .stream()
                .map(this::mapVehicleToResponse)
                .collect(Collectors.toList());

        log.info("Found {} vehicle(s) for driver profile ID: {}", vehicles.size(), driverProfileId);

        return DriverVehicleListResponse.builder()
                .driverProfileId(driverProfileId)
                .totalVehicles(vehicles.size())
                .hasMultipleVehicles(vehicles.size() > 1)
                .vehicles(vehicles)
                .build();
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

