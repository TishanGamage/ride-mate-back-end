package com.ride.mate.service;

import com.ride.mate.domain.User;
import com.ride.mate.domain.UserProfile;
import com.ride.mate.enums.UserRole;
import com.ride.mate.enums.YesNo;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.*;
import com.ride.mate.resources.*;
import com.ride.mate.service.impl.UserProfileServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * UserProfileServiceTests
 * JUnit test cases for UserProfileService business logic
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
public class UserProfileServiceTests {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DocumentDetailsRepository documentDetailsRepository;

    @Mock
    private UserIdentificationDetailsRepository userIdentificationDetailsRepository;

    @Mock
    private IdentificationTypeRepository identificationTypeRepository;

    @Mock
    private EmergencyContactRepository emergencyContactRepository;

    @Mock
    private DriverProfileService driverProfileService;

    @Mock
    private Environment environment;

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    private UserProfileAddResource userProfileAddRequest;
    private UserProfileUpdateResource userProfileUpdateRequest;
    private User mockUser;
    private UserProfile mockUserProfile;

    @Before
    public void setUp() {
        // Setup user profile add request
        userProfileAddRequest = new UserProfileAddResource();
        userProfileAddRequest.setUserId(1L);
        userProfileAddRequest.setDateOfBirth("1995-05-15");
        userProfileAddRequest.setGender("Male");
        userProfileAddRequest.setWillingToDrive(YesNo.NO);

        // Setup user profile update request
        userProfileUpdateRequest = new UserProfileUpdateResource();
        userProfileUpdateRequest.setDateOfBirth("1995-05-15");
        userProfileUpdateRequest.setGender("Male");
        userProfileUpdateRequest.setBio("Experienced driver");

        // Setup mock user
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setUserRole(UserRole.PASSENGER);

        // Setup mock user profile
        mockUserProfile = new UserProfile();
        mockUserProfile.setId(1L);
        mockUserProfile.setUser(mockUser);
        mockUserProfile.setDateOfBirth(LocalDate.parse("1995-05-15"));
        mockUserProfile.setGender("Male");
        mockUserProfile.setBio("Test bio");
        mockUserProfile.setWillingToDrive(YesNo.NO);
        mockUserProfile.setUserProfileCompleted("NO");
        mockUserProfile.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
        mockUserProfile.setCreatedUser("SYSTEM");
    }

    @Test
    public void testCreateUserProfile_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userProfileRepository.existsByUserId(1L)).thenReturn(false);
        when(userProfileRepository.saveAndFlush(any(UserProfile.class))).thenReturn(mockUserProfile);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(mockUserProfile);

        // Act
        UserProfile result = userProfileService.createUserProfile(userProfileAddRequest);

        // Assert
        assertNotNull(result);
        assertEquals(Optional.of(1L), result.getId());
        verify(userProfileRepository, times(1)).saveAndFlush(any(UserProfile.class));
    }

    @Test
    public void testCreateUserProfile_UserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(environment.getProperty(anyString())).thenReturn("User not found");

        // Act & Assert
        assertThrows(ValidateRecordException.class, () -> {
            userProfileService.createUserProfile(userProfileAddRequest);
        });

        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    public void testCreateUserProfile_ProfileAlreadyExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userProfileRepository.existsByUserId(1L)).thenReturn(true);
        when(environment.getProperty(anyString())).thenReturn("Profile already exists");

        // Act & Assert
        assertThrows(ValidateRecordException.class, () -> {
            userProfileService.createUserProfile(userProfileAddRequest);
        });

        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    public void testUpdateUserProfile_Success() {
        // Arrange
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(mockUserProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(mockUserProfile);

        // Act
        UserProfile result = userProfileService.updateUserProfile(userProfileUpdateRequest, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(Optional.of(1L), result.getId());
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    public void testUpdateUserProfile_ProfileNotFound() {
        // Arrange
        when(userProfileRepository.findById(1L)).thenReturn(Optional.empty());
        when(environment.getProperty(anyString())).thenReturn("Profile not found");

        // Act & Assert
        assertThrows(ValidateRecordException.class, () -> {
            userProfileService.updateUserProfile(userProfileUpdateRequest, 1L);
        });

        verify(userProfileRepository, never()).save(any(UserProfile.class));
    }

    @Test
    public void testGetUserProfileByUserId_Success() {
        // Arrange
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(mockUserProfile));

        // Act
        UserProfileResponse result = userProfileService.getUserProfileByUserId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(Optional.of(1L), result.getUserId());
        verify(userProfileRepository, times(1)).findByUserId(1L);
    }

    @Test
    public void testGetUserProfileByUserId_NotFound() {
        // Arrange
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(environment.getProperty(anyString())).thenReturn("Profile not found");

        // Act & Assert
        assertThrows(ValidateRecordException.class, () -> {
            userProfileService.getUserProfileByUserId(1L);
        });
    }

    @Test
    public void testUpdateWillingToDrive_Success() {
        // Arrange
        WillingToDriveUpdateResource request = new WillingToDriveUpdateResource();
        request.setWillingToDrive(YesNo.YES);

        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(mockUserProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(mockUserProfile);

        // Act
        UserProfile result = userProfileService.updateWillingToDrive(1L, request);

        // Assert
        assertNotNull(result);
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    public void testUpdateProfilePhoto_Success() {
        // Arrange
        ProfilePhotoUpdateResource request = new ProfilePhotoUpdateResource();
        request.setProfileImageDocumentId(10L);

        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(mockUserProfile));
        when(documentDetailsRepository.findById(10L)).thenReturn(Optional.of(new com.ride.mate.domain.DocumentDetails()));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(mockUserProfile);

        // Act
        UserProfile result = userProfileService.updateProfilePhoto(1L, request);

        // Assert
        assertNotNull(result);
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    public void testUpdateRole_Success() {
        // Arrange
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRole("DRIVER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(environment.getProperty(anyString())).thenReturn("Role updated successfully");

        // Act
        SuccessAndErrorDetailsResource result = userProfileService.updateRole(1L, request);

        // Assert
        assertNotNull(result);
        assertEquals("Role updated successfully", result.getMessages());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testUpdateRole_InvalidRole() {
        // Arrange
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRole("INVALID_ROLE");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(environment.getProperty(anyString())).thenReturn("Invalid role");

        // Act & Assert
        assertThrows(ValidateRecordException.class, () -> {
            userProfileService.updateRole(1L, request);
        });

        verify(userRepository, never()).save(any(User.class));
    }
}

