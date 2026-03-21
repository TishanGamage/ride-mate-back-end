package com.ride.mate.service;

import com.ride.mate.domain.*;
import com.ride.mate.enums.UserRole;
import com.ride.mate.enums.UserStatus;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.*;
import com.ride.mate.resources.AvailableRideResponse;
import com.ride.mate.resources.RideRequestResource;
import com.ride.mate.resources.RideRequestResponse;
import com.ride.mate.service.impl.RideRequestServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RideRequestServiceTests
 * JUnit test cases for RideRequestService business logic
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
public class RideRequestServiceTests {

    @Mock
    private RideRequestRepository rideRequestRepository;

    @Mock
    private RideDetailRepository rideDetailRepository;

    @Mock
    private ShareRideDetailRepository shareRideDetailRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DriverProfileRepository driverProfileRepository;


    @Mock
    private Environment environment;

    @InjectMocks
    private RideRequestServiceImpl rideRequestService;

    private RideRequestResource rideRequestResource;
    private RideDetail mockRideDetail;
    private User mockUser;
    private RideRequest mockRideRequest;
    private DriverProfile mockDriverProfile;

    @Before
    public void setUp() {
        // Setup ride request resource
        rideRequestResource = new RideRequestResource();
        rideRequestResource.setRideDetailId(1L);
        rideRequestResource.setUserId(1L);
        rideRequestResource.setPassengerStartLat(new BigDecimal("6.9271"));
        rideRequestResource.setPassengerStartLng(new BigDecimal("79.8612"));
        rideRequestResource.setPassengerEndLat(new BigDecimal("6.8300"));
        rideRequestResource.setPassengerEndLng(new BigDecimal("79.9200"));
        rideRequestResource.setPassengerRideDistance(new BigDecimal("15.5"));
        rideRequestResource.setStartCity("Colombo");
        rideRequestResource.setEndCity("Moratuwa");

        // Setup mock driver profile
        mockDriverProfile = new DriverProfile();
        mockDriverProfile.setId(1L);

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
        mockRideDetail.setTotalRideCost(new BigDecimal("500.00"));
        mockRideDetail.setAvailableSeats(3L);
        mockRideDetail.setStatus("ACTIVE");

        // Setup mock user
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setPhoneNumber("1234567890");
        mockUser.setUserRole(UserRole.PASSENGER);
        mockUser.setStatus(UserStatus.ACTIVE);

        // Setup mock ride request
        mockRideRequest = new RideRequest();
        mockRideRequest.setId(1L);
        mockRideRequest.setRideDetail(mockRideDetail);
        mockRideRequest.setUser(mockUser);
        mockRideRequest.setPassengerStartLat(new BigDecimal("6.9271"));
        mockRideRequest.setPassengerStartLng(new BigDecimal("79.8612"));
        mockRideRequest.setPassengerEndLat(new BigDecimal("6.8300"));
        mockRideRequest.setPassengerEndLng(new BigDecimal("79.9200"));
        mockRideRequest.setStartCity("Colombo");
        mockRideRequest.setEndCity("Moratuwa");
        mockRideRequest.setPassengerRideDistance(new BigDecimal("15.5"));
        mockRideRequest.setStatus("PENDING");
        mockRideRequest.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
        mockRideRequest.setCreatedUser("SYSTEM");
    }

    @Test
    public void testGetAvailableRides_Success() {
        // Arrange
        when(rideDetailRepository.findByStatus("ACTIVE")).thenReturn(Collections.singletonList(mockRideDetail));

        // Act
        List<AvailableRideResponse> result = rideRequestService.getAvailableRides(null, null, null);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(rideDetailRepository, times(1)).findByStatus("ACTIVE");
    }

    @Test
    public void testGetAvailableRides_NoActiveRides() {
        // Arrange
        when(rideDetailRepository.findByStatus("ACTIVE")).thenReturn(Collections.emptyList());

        // Act
        List<AvailableRideResponse> result = rideRequestService.getAvailableRides(null, null, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(rideDetailRepository, times(1)).findByStatus("ACTIVE");
    }

    @Test
    public void testCreateRideRequest_Success() {
        // Arrange
        when(rideDetailRepository.findById(1L)).thenReturn(Optional.of(mockRideDetail));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(shareRideDetailRepository.countByRideDetailIdAndStatus(anyLong(), anyString())).thenReturn(1L);
        when(rideRequestRepository.existsByRideDetailIdAndUserIdAndStatusIn(anyLong(), anyLong(), anyList()))
                .thenReturn(false);
        when(rideRequestRepository.save(any(RideRequest.class))).thenReturn(mockRideRequest);

        // Act
        RideRequestResponse result = rideRequestService.createRideRequest(rideRequestResource);

        // Assert
        assertNotNull(result);
        assertEquals(Long.valueOf(1L), result.getId());
        assertEquals(Long.valueOf(1L), result.getRideDetailId());
        assertEquals(Long.valueOf(1L), result.getUserId());
        assertEquals("PENDING", result.getStatus());
        
        verify(rideRequestRepository, times(1)).save(any(RideRequest.class));
    }

    @Test
    public void testCreateRideRequest_RideNotFound() {
        // Arrange
        when(rideDetailRepository.findById(1L)).thenReturn(Optional.empty());
        when(environment.getProperty(anyString())).thenReturn("Ride not found");

        // Act & Assert
        assertThrows(ValidateRecordException.class, 
            () -> rideRequestService.createRideRequest(rideRequestResource));

        verify(rideRequestRepository, never()).save(any(RideRequest.class));
    }

    @Test
    public void testCreateRideRequest_RideNotActive() {
        // Arrange
        mockRideDetail.setStatus("COMPLETED");
        when(rideDetailRepository.findById(1L)).thenReturn(Optional.of(mockRideDetail));
        when(environment.getProperty(anyString())).thenReturn("Ride not available");

        // Act & Assert
        assertThrows(ValidateRecordException.class, 
            () -> rideRequestService.createRideRequest(rideRequestResource));

        verify(rideRequestRepository, never()).save(any(RideRequest.class));
    }

    @Test
    public void testCreateRideRequest_NoAvailableSeats() {
        // Arrange
        mockRideDetail.setAvailableSeats(2L);
        when(rideDetailRepository.findById(1L)).thenReturn(Optional.of(mockRideDetail));
        when(shareRideDetailRepository.countByRideDetailIdAndStatus(1L, "ACTIVE")).thenReturn(2L);
        when(environment.getProperty(anyString())).thenReturn("No available seats");

        // Act & Assert
        assertThrows(ValidateRecordException.class, 
            () -> rideRequestService.createRideRequest(rideRequestResource));

        verify(rideRequestRepository, never()).save(any(RideRequest.class));
    }

    @Test
    public void testCreateRideRequest_AlreadyRequested() {
        // Arrange
        when(rideDetailRepository.findById(1L)).thenReturn(Optional.of(mockRideDetail));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(shareRideDetailRepository.countByRideDetailIdAndStatus(anyLong(), anyString())).thenReturn(1L);
        when(rideRequestRepository.existsByRideDetailIdAndUserIdAndStatusIn(anyLong(), anyLong(), anyList()))
                .thenReturn(true);
        when(environment.getProperty(anyString())).thenReturn("Request already pending");

        // Act & Assert
        assertThrows(ValidateRecordException.class, 
            () -> rideRequestService.createRideRequest(rideRequestResource));

        verify(rideRequestRepository, never()).save(any(RideRequest.class));
    }

    @Test
    public void testGetPendingRequestsForDriver_Success() {
        // Arrange
        when(driverProfileRepository.findById(1L)).thenReturn(Optional.of(mockDriverProfile));
        when(rideRequestRepository.findByDriverProfileIdAndStatus(1L, "PENDING"))
                .thenReturn(Collections.singletonList(mockRideRequest));

        // Act
        List<RideRequestResponse> result = rideRequestService.getPendingRequestsForDriver(1L);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(rideRequestRepository, times(1)).findByDriverProfileIdAndStatus(1L, "PENDING");
    }

    @Test
    public void testGetPendingRequestsForDriver_DriverNotFound() {
        // Arrange
        when(driverProfileRepository.findById(1L)).thenReturn(Optional.empty());
        when(environment.getProperty(anyString())).thenReturn("Driver profile not found");

        // Act & Assert
        assertThrows(ValidateRecordException.class, 
            () -> rideRequestService.getPendingRequestsForDriver(1L));

        verify(rideRequestRepository, never()).findByDriverProfileIdAndStatus(anyLong(), anyString());
    }

    @Test
    public void testAcceptRideRequest_Success() {
        // Arrange
        when(rideRequestRepository.findById(1L)).thenReturn(Optional.of(mockRideRequest));
        when(shareRideDetailRepository.countByRideDetailIdAndStatus(anyLong(), anyString())).thenReturn(1L);
        when(rideRequestRepository.save(any(RideRequest.class))).thenReturn(mockRideRequest);
        when(shareRideDetailRepository.save(any(ShareRideDetail.class))).thenReturn(new ShareRideDetail());

        // Act
        RideRequestResponse result = rideRequestService.acceptRideRequest(1L);

        // Assert
        assertNotNull(result);
        verify(rideRequestRepository, times(1)).save(any(RideRequest.class));
        verify(shareRideDetailRepository, times(1)).save(any(ShareRideDetail.class));
    }

    @Test
    public void testAcceptRideRequest_NotPending() {
        // Arrange
        mockRideRequest.setStatus("ACCEPTED");
        when(rideRequestRepository.findById(1L)).thenReturn(Optional.of(mockRideRequest));
        when(environment.getProperty(anyString())).thenReturn("Request already processed");

        // Act & Assert
        assertThrows(ValidateRecordException.class, 
            () -> rideRequestService.acceptRideRequest(1L));

        verify(shareRideDetailRepository, never()).save(any(ShareRideDetail.class));
    }

    @Test
    public void testRejectRideRequest_Success() {
        // Arrange
        when(rideRequestRepository.findById(1L)).thenReturn(Optional.of(mockRideRequest));
        mockRideRequest.setStatus("REJECTED");
        when(rideRequestRepository.save(any(RideRequest.class))).thenReturn(mockRideRequest);

        // Act
        RideRequestResponse result = rideRequestService.rejectRideRequest(1L);

        // Assert
        assertNotNull(result);
        assertEquals("REJECTED", result.getStatus());
        verify(rideRequestRepository, times(1)).save(any(RideRequest.class));
    }

    @Test
    public void testAcceptRideRequest_RequestNotFound() {
        // Arrange
        when(rideRequestRepository.findById(1L)).thenReturn(Optional.empty());
        when(environment.getProperty(anyString())).thenReturn("Ride request not found");

        // Act & Assert
        assertThrows(ValidateRecordException.class, 
            () -> rideRequestService.acceptRideRequest(1L));

        verify(rideRequestRepository, never()).save(any(RideRequest.class));
    }
}
