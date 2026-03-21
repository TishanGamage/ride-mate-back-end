package com.ride.mate.service;

import com.ride.mate.domain.*;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.*;
import com.ride.mate.resources.DriverProfileRequestResource;
import com.ride.mate.resources.DriverProfileResponse;
import com.ride.mate.resources.DriverVehicleDetailsRequestResource;
import com.ride.mate.service.impl.DriverProfileServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * DriverProfileServiceTests
 * JUnit test cases for DriverProfileService business logic
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 21-03-2026    N/A          N/A          Iruni          Initial Development
 */
@RunWith(MockitoJUnitRunner.class)
public class DriverProfileServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DriverProfileRepository driverProfileRepository;

    @Mock
    private DriverVehicleDetailsRepository driverVehicleDetailsRepository;

    @Mock
    private VehicleTypeRepository vehicleTypeRepository;

    @Mock
    private VehicleMakeRepository vehicleMakeRepository;

    @Mock
    private VehicleModelRepository vehicleModelRepository;

    @Mock
    private DocumentDetailsRepository documentDetailsRepository;

    @Mock
    private DriverWalletService driverWalletService;

    @Mock
    private Environment environment;

    @InjectMocks
    private DriverProfileServiceImpl driverProfileService;

    private DriverProfileRequestResource driverProfileRequest;
    private User mockUser;
    private DriverProfile mockDriverProfile;
    private VehicleType mockVehicleType;
    private VehicleMake mockVehicleMake;
    private VehicleModel mockVehicleModel;

    @Before
    public void setUp() {
        // Setup driver profile request
        driverProfileRequest = new DriverProfileRequestResource();
        driverProfileRequest.setDriverLicenseNumber("DL123456789");
        driverProfileRequest.setDriverLicenseExpiry("2030-12-31");

        // Setup vehicle details request
        DriverVehicleDetailsRequestResource vehicleDetails = new DriverVehicleDetailsRequestResource();
        vehicleDetails.setVehicleMakeId(1L);
        vehicleDetails.setVehicleModelId(1L);
        vehicleDetails.setVehicleTypeId(1L);
        vehicleDetails.setRegistrationNumber("ABC-1234");
        vehicleDetails.setYear(2020);
        vehicleDetails.setColor("Black");
        vehicleDetails.setSeats(4);
        driverProfileRequest.setVehicleDetails(vehicleDetails);

        // Setup mock user
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setFirstName("John");

        // Setup mock driver profile
        mockDriverProfile = new DriverProfile();
        mockDriverProfile.setId(1L);
        mockDriverProfile.setUser(mockUser);
        mockDriverProfile.setDriverLicenseNumber("DL123456789");
        mockDriverProfile.setDriverLicenseExpiry(LocalDate.parse("2030-12-31"));
        mockDriverProfile.setAccountStatus("PENDING");

        // Setup mock vehicle type
        mockVehicleType = new VehicleType();
        mockVehicleType.setId(1L);
        mockVehicleType.setName("Sedan");
        mockVehicleType.setPerKmRate(new java.math.BigDecimal("50.00"));

        // Setup mock vehicle make
        mockVehicleMake = new VehicleMake();
        mockVehicleMake.setId(1L);
        mockVehicleMake.setName("Toyota");

        // Setup mock vehicle model
        mockVehicleModel = new VehicleModel();
        mockVehicleModel.setId(1L);
        mockVehicleModel.setName("Camry");
    }

    @Test
    public void testSaveDriverProfile_NewProfile_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(driverProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(driverProfileRepository.existsByDriverLicenseNumber(anyString())).thenReturn(false);
        when(driverProfileRepository.saveAndFlush(any(DriverProfile.class))).thenReturn(mockDriverProfile);
        when(vehicleTypeRepository.findById(1L)).thenReturn(Optional.of(mockVehicleType));
        when(vehicleMakeRepository.findById(1L)).thenReturn(Optional.of(mockVehicleMake));
        when(vehicleModelRepository.findById(1L)).thenReturn(Optional.of(mockVehicleModel));
        when(driverVehicleDetailsRepository.save(any(DriverVehicleDetails.class)))
                .thenReturn(new DriverVehicleDetails());
        doNothing().when(driverWalletService).initializeWallet(any(DriverProfile.class));

        // Act
        DriverProfile result = driverProfileService.saveDriverProfile(1L, driverProfileRequest);

        // Assert
        assertNotNull(result);
        assertEquals(Optional.of(1L), result.getId());
        verify(driverProfileRepository, times(1)).saveAndFlush(any(DriverProfile.class));
        verify(driverWalletService, times(1)).initializeWallet(any(DriverProfile.class));
    }

    @Test
    public void testSaveDriverProfile_UpdateExisting_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(driverProfileRepository.findByUserId(1L)).thenReturn(Optional.of(mockDriverProfile));
        when(driverProfileRepository.saveAndFlush(any(DriverProfile.class))).thenReturn(mockDriverProfile);
        when(vehicleTypeRepository.findById(1L)).thenReturn(Optional.of(mockVehicleType));
        when(vehicleMakeRepository.findById(1L)).thenReturn(Optional.of(mockVehicleMake));
        when(vehicleModelRepository.findById(1L)).thenReturn(Optional.of(mockVehicleModel));
        when(driverVehicleDetailsRepository.save(any(DriverVehicleDetails.class)))
                .thenReturn(new DriverVehicleDetails());

        // Act
        DriverProfile result = driverProfileService.saveDriverProfile(1L, driverProfileRequest);

        // Assert
        assertNotNull(result);
        verify(driverProfileRepository, times(1)).saveAndFlush(any(DriverProfile.class));
        verify(driverWalletService, never()).initializeWallet(any(DriverProfile.class));
    }

    @Test
    public void testSaveDriverProfile_UserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(environment.getProperty(anyString())).thenReturn("User not found");

        // Act & Assert
        assertThrows(ValidateRecordException.class, () -> {
            driverProfileService.saveDriverProfile(1L, driverProfileRequest);
        });

        verify(driverProfileRepository, never()).save(any(DriverProfile.class));
    }

    @Test
    public void testSaveDriverProfile_LicenseNumberExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(driverProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(driverProfileRepository.existsByDriverLicenseNumber(anyString())).thenReturn(true);
        when(environment.getProperty(anyString())).thenReturn("License number already exists");

        // Act & Assert
        assertThrows(ValidateRecordException.class, () -> {
            driverProfileService.saveDriverProfile(1L, driverProfileRequest);
        });

        verify(driverProfileRepository, never()).save(any(DriverProfile.class));
    }

    @Test
    public void testGetDriverProfileByUserId_Success() {
        // Arrange
        when(driverProfileRepository.findByUserId(1L)).thenReturn(Optional.of(mockDriverProfile));
        when(driverVehicleDetailsRepository.findByDriverProfileId(1L))
                .thenReturn(Arrays.asList(new DriverVehicleDetails()));

        // Act
        DriverProfileResponse result = driverProfileService.getDriverProfileByUserId(1L);

        // Assert
        assertNotNull(result);
        assertEquals((Long)1L, result.getId());
        verify(driverProfileRepository, times(1)).findByUserId(1L);
    }

    @Test
    public void testGetDriverProfileByUserId_NotFound() {
        // Arrange
        when(driverProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(environment.getProperty(anyString())).thenReturn("Driver profile not found");

        // Act & Assert
        assertThrows(ValidateRecordException.class, () -> {
            driverProfileService.getDriverProfileByUserId(1L);
        });
    }
}

