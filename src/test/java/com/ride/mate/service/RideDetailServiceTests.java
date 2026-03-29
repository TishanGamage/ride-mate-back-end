package com.ride.mate.service;

import com.ride.mate.domain.*;
import com.ride.mate.enums.RideStatus;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.*;
import com.ride.mate.resources.RideDetailRequestResource;
import com.ride.mate.resources.RideDetailResponseResource;
import com.ride.mate.resources.RidePriceCalculationResponse;
import com.ride.mate.service.impl.RideDetailServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * RideDetailServiceTests
 * JUnit test cases for RideDetailService business logic
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
public class RideDetailServiceTests {

    @Mock
    private RideDetailRepository rideDetailRepository;

    @Mock
    private DriverProfileRepository driverProfileRepository;

    @Mock
    private DriverVehicleDetailsRepository driverVehicleDetailsRepository;


    @Mock
    private Environment environment;

    @InjectMocks
    private RideDetailServiceImpl rideDetailService;

    private RideDetailRequestResource rideDetailRequest;
    private DriverProfile mockDriverProfile;
    private RideDetail mockRideDetail;
    private DriverVehicleDetails mockVehicleDetails;

    @Before
    public void setUp() {
        // Setup ride detail request
        rideDetailRequest = new RideDetailRequestResource();
        rideDetailRequest.setDriverProfileId(1L);
        rideDetailRequest.setStartLocationLatitude(new BigDecimal("6.9271"));
        rideDetailRequest.setStartLocationLongitude(new BigDecimal("79.8612"));
        rideDetailRequest.setEndLocationLatitude(new BigDecimal("6.8300"));
        rideDetailRequest.setEndLocationLongitude(new BigDecimal("79.9200"));
        rideDetailRequest.setStartCity("Colombo");
        rideDetailRequest.setEndCity("Moratuwa");
        rideDetailRequest.setTotalRideDistance(new BigDecimal("15.5"));
        rideDetailRequest.setAvailableSeats(3L);
        rideDetailRequest.setPerKmRate(new BigDecimal("50.00"));
        rideDetailRequest.setTotalRideCost(new BigDecimal("775.00"));

        // Setup mock driver profile
        mockDriverProfile = new DriverProfile();
        mockDriverProfile.setId(1L);
        mockDriverProfile.setDriverLicenseNumber("DL123456");

        // Setup mock vehicle type
        VehicleType mockVehicleType = new VehicleType();
        mockVehicleType.setId(1L);
        mockVehicleType.setName("Sedan");
        mockVehicleType.setPerKmRate(new BigDecimal("50.00"));

        // Setup mock vehicle details
        mockVehicleDetails = new DriverVehicleDetails();
        mockVehicleDetails.setId(1L);
        mockVehicleDetails.setDriverProfile(mockDriverProfile);
        mockVehicleDetails.setVehicleType(mockVehicleType);
        mockVehicleDetails.setRegistrationNumber("ABC-1234");

        // Setup mock ride detail
        mockRideDetail = new RideDetail();
        mockRideDetail.setId(1L);
        mockRideDetail.setDriverProfile(mockDriverProfile);
        mockRideDetail.setStartLocationLatitude(new BigDecimal("6.9271"));
        mockRideDetail.setStartLocationLongitude(new BigDecimal("79.8612"));
        mockRideDetail.setEndLocationLatitude(new BigDecimal("6.8300"));
        mockRideDetail.setEndLocationLongitude(new BigDecimal("79.9200"));
        mockRideDetail.setStartCity("Colombo");
        mockRideDetail.setEndCity("Moratuwa");
        mockRideDetail.setTotalRideDistance(new BigDecimal("15.5"));
        mockRideDetail.setAvailableSeats(3L);
        mockRideDetail.setStatus(RideStatus.ACTIVE);
    }

    @Test
    public void testCreateRideDetail_Success() {
        // Arrange
        when(driverProfileRepository.findById(1L)).thenReturn(Optional.of(mockDriverProfile));
        when(rideDetailRepository.existsRideDetailByDriverProfileIdAndStatus(1L, RideStatus.ACTIVE)).thenReturn(false);
        when(rideDetailRepository.save(any(RideDetail.class))).thenReturn(mockRideDetail);

        // Act
        RideDetail result = rideDetailService.createRideDetail(rideDetailRequest);

        // Assert
        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
        verify(rideDetailRepository, times(1)).save(any(RideDetail.class));
    }

    @Test
    public void testCreateRideDetail_DriverProfileNotFound() {
        // Arrange
        when(driverProfileRepository.findById(1L)).thenReturn(Optional.empty());
        when(environment.getProperty(anyString())).thenReturn("Driver profile not found");

        // Act & Assert
        assertThrows(ValidateRecordException.class,
                () -> rideDetailService.createRideDetail(rideDetailRequest));

        verify(rideDetailRepository, never()).save(any(RideDetail.class));
    }

    @Test
    public void testCreateRideDetail_ActiveRideExists() {
        // Arrange
        when(driverProfileRepository.findById(1L)).thenReturn(Optional.of(mockDriverProfile));
        when(rideDetailRepository.existsRideDetailByDriverProfileIdAndStatus(1L, RideStatus.ACTIVE)).thenReturn(true);
        when(environment.getProperty(anyString())).thenReturn("Active ride already exists");

        // Act & Assert
        assertThrows(ValidateRecordException.class,
                () -> rideDetailService.createRideDetail(rideDetailRequest));

        verify(rideDetailRepository, never()).save(any(RideDetail.class));
    }

    @Test
    public void testCalculateRidePrice_Success() {
        // Arrange
        when(driverProfileRepository.findById(1L)).thenReturn(Optional.of(mockDriverProfile));
        when(driverVehicleDetailsRepository.findByDriverProfileIdAndIsPrimary(anyLong(), any()))
                .thenReturn(Optional.of(mockVehicleDetails));

        // Act
        RidePriceCalculationResponse result = rideDetailService.calculateRidePrice(1L, new BigDecimal("15.5"));

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("50.00"), result.getPerKmRate());
        assertEquals(new BigDecimal("15.5"), result.getTotalDistance());
        assertEquals("Sedan", result.getVehicleTypeName());
        
        verify(driverProfileRepository, times(1)).findById(1L);
    }

    @Test
    public void testCalculateRidePrice_NoPrimaryVehicle() {
        // Arrange
        when(driverProfileRepository.findById(1L)).thenReturn(Optional.of(mockDriverProfile));
        when(driverVehicleDetailsRepository.findByDriverProfileIdAndIsPrimary(anyLong(), any()))
                .thenReturn(Optional.empty());
        when(environment.getProperty(anyString())).thenReturn("No primary vehicle found");

        // Act & Assert
        assertThrows(ValidateRecordException.class,
                () -> rideDetailService.calculateRidePrice(1L, new BigDecimal("15.5")));
    }

    @Test
    public void testGetDriverRides_Success() {
        // Arrange
        when(driverProfileRepository.findById(1L)).thenReturn(Optional.of(mockDriverProfile));
        when(rideDetailRepository.findByDriverProfileId(1L)).thenReturn(List.of(mockRideDetail));

        // Act
        List<RideDetailResponseResource> result = rideDetailService.getRidesByDriverProfileId(1L, null);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(rideDetailRepository, times(1)).findByDriverProfileId(1L);
    }

    @Test
    public void testGetDriverRides_DriverProfileNotFound() {
        // Arrange
        when(driverProfileRepository.findById(1L)).thenReturn(Optional.empty());
        when(environment.getProperty(anyString())).thenReturn("Driver profile not found");

        // Act & Assert
        assertThrows(ValidateRecordException.class,
                () -> rideDetailService.getRidesByDriverProfileId(1L, null));

        verify(rideDetailRepository, never()).findByDriverProfileId(anyLong());
    }
}
