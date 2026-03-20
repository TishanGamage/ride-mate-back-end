package com.ride.mate.service;

import com.ride.mate.domain.User;
import com.ride.mate.enums.UserRole;
import com.ride.mate.enums.UserStatus;
import com.ride.mate.enums.YesNo;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.UserRepository;
import com.ride.mate.resources.LoginResponse;
import com.ride.mate.resources.UserRegistrationAddResource;
import com.ride.mate.resources.UserRegistrationUpdateResource;
import com.ride.mate.service.impl.UserServiceImpl;
import com.ride.mate.util.JwtUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * UserServiceTests
 * JUnit test cases for UserService business logic
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
public class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Environment environment;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegistrationAddResource registrationRequest;
    private UserRegistrationUpdateResource updateRequest;
    private User mockUser;

    @Before
    public void setUp() {
        // Setup registration request
        registrationRequest = new UserRegistrationAddResource();
        registrationRequest.setEmail("test@example.com");
        registrationRequest.setFirstName("John");
        registrationRequest.setLastName("Doe");
        registrationRequest.setPhoneNumber("1234567890");
        registrationRequest.setPassword("password123");
        registrationRequest.setUserRole(UserRole.PASSENGER);

        // Setup update request
        updateRequest = new UserRegistrationUpdateResource();
        updateRequest.setId(1L);
        updateRequest.setEmail("updated@example.com");
        updateRequest.setFirstName("John");
        updateRequest.setLastName("Doe");
        updateRequest.setPhoneNumber("0987654321");
        updateRequest.setPassword("newPassword123");
        updateRequest.setUserRole(UserRole.PASSENGER);
        updateRequest.setVersion("1");

        // Setup mock user
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setPhoneNumber("1234567890");
        mockUser.setPasswordHash("hashedPassword");
        mockUser.setUserRole(UserRole.PASSENGER);
        mockUser.setStatus(UserStatus.PENDING);
        mockUser.setEmailVerified(YesNo.NO);
        mockUser.setVersion(1L);
        mockUser.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
        mockUser.setCreatedUser("SYSTEM");
    }

    @Test
    public void testRegisterUser_Success() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtUtil.generateAccessToken(any(), anyString(), anyString(), anyString())).thenReturn("mock-access-token");
        when(jwtUtil.generateRefreshToken(any(), anyString())).thenReturn("mock-refresh-token");
        when(environment.getProperty(anyString())).thenReturn("Record created successfully");

        // Act
        LoginResponse response = userService.registerUser(registrationRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("mock-access-token", response.getAccessToken());
        assertEquals("mock-refresh-token", response.getRefreshToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("PASSENGER", response.getRole());
        
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    public void testRegisterUser_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        when(environment.getProperty(anyString())).thenReturn("Email already exists");

        // Act & Assert
        assertThrows(ValidateRecordException.class, () -> {
            userService.registerUser(registrationRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testRegisterUser_PhoneNumberAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(true);
        when(environment.getProperty(anyString())).thenReturn("Phone number already exists");

        // Act & Assert
        assertThrows(ValidateRecordException.class, () -> {
            userService.registerUser(registrationRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testUpdateUser_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Act
        User updatedUser = userService.updateUser(updateRequest);

        // Assert
        assertNotNull(updatedUser);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testUpdateUser_UserNotFound() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(environment.getProperty(anyString())).thenReturn("Record not found");

        // Act & Assert
        assertThrows(ValidateRecordException.class, () -> {
            userService.updateUser(updateRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testUpdateUser_EmailAlreadyExists() {
        // Arrange
        mockUser.setEmail("old@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(true);
        when(environment.getProperty(anyString())).thenReturn("Email already exists");

        // Act & Assert
        assertThrows(ValidateRecordException.class, () -> {
            userService.updateUser(updateRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testUpdateUser_VersionMismatch() {
        // Arrange
        mockUser.setVersion(2L);
        updateRequest.setVersion("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(environment.getProperty(anyString())).thenReturn("Version mismatch");

        // Act & Assert
        assertThrows(ValidateRecordException.class, () -> {
            userService.updateUser(updateRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }
}
