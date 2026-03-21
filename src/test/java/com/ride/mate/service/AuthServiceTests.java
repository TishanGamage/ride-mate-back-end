package com.ride.mate.service;

import com.ride.mate.domain.User;
import com.ride.mate.domain.VerificationCode;
import com.ride.mate.enums.UserRole;
import com.ride.mate.enums.UserStatus;
import com.ride.mate.enums.YesNo;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.UserRepository;
import com.ride.mate.resources.*;
import com.ride.mate.service.impl.AuthServiceImpl;
import com.ride.mate.util.JwtUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AuthServiceTests
 * JUnit test cases for AuthService business logic
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
public class AuthServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationCodeService verificationCodeService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Environment environment;

    @InjectMocks
    private AuthServiceImpl authService;

    private LoginRequest loginRequest;
    private SendVerificationCodeRequest verificationCodeRequest;
    private VerifyCodeRequest verifyCodeRequest;
    private User mockUser;
    private VerificationCode mockVerificationCode;

    @Before
    public void setUp() {
        // Set access token expiration for testing
        ReflectionTestUtils.setField(authService, "accessTokenExpiration", 3600000L);

        // Setup login request
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        // Setup verification code request
        verificationCodeRequest = new SendVerificationCodeRequest();
        verificationCodeRequest.setEmail("test@example.com");

        // Setup verify code request
        verifyCodeRequest = new VerifyCodeRequest();
        verifyCodeRequest.setEmail("test@example.com");
        verifyCodeRequest.setCode("123456");

        // Setup mock user
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setPhoneNumber("1234567890");
        mockUser.setPasswordHash("hashedPassword");
        mockUser.setUserRole(UserRole.PASSENGER);
        mockUser.setStatus(UserStatus.ACTIVE);
        mockUser.setEmailVerified(YesNo.YES);
        mockUser.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
        mockUser.setCreatedUser("SYSTEM");

        // Setup mock verification code
        mockVerificationCode = new VerificationCode();
        mockVerificationCode.setId(1L);
        mockVerificationCode.setEmail("test@example.com");
        mockVerificationCode.setCode("123456");
        mockVerificationCode.setExpiryTime(Timestamp.valueOf(LocalDateTime.now().plusMinutes(10)).toLocalDateTime());
    }

    @Test
    public void testSendVerificationCode_Success() {
        // Arrange
        when(verificationCodeService.sendVerificationCode(any(SendVerificationCodeRequest.class)))
                .thenReturn(mockVerificationCode);

        // Act
        VerificationCode result = authService.sendVerificationCode(verificationCodeRequest);

        // Assert
        assertNotNull(result);
        assertEquals("123456", result.getCode());
        assertEquals("test@example.com", result.getEmail());
        verify(verificationCodeService, times(1)).sendVerificationCode(any(SendVerificationCodeRequest.class));
    }

    @Test
    public void testVerifyCode_Success() {
        // Arrange
        SuccessAndErrorDetailsResource mockResponse = new SuccessAndErrorDetailsResource();
        mockResponse.setIsValid(true);
        mockResponse.setMessages("Verification successful");

        when(verificationCodeService.verifyCode(any(VerifyCodeRequest.class))).thenReturn(mockResponse);

        // Act
        SuccessAndErrorDetailsResource result = authService.verifyCode(verifyCodeRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.getIsValid());
        verify(verificationCodeService, times(1)).verifyCode(any(VerifyCodeRequest.class));
    }

    @Test
    public void testLoginUser_Success() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateAccessToken(any(), anyString(), anyString(), anyString())).thenReturn("mock-access-token");
        when(jwtUtil.generateRefreshToken(any(), anyString())).thenReturn("mock-refresh-token");
        when(environment.getProperty(anyString())).thenReturn("Login successful");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // Act
        LoginResponse response = authService.loginUser(loginRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("mock-access-token", response.getAccessToken());
        assertEquals("mock-refresh-token", response.getRefreshToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("PASSENGER", response.getRole());
        
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    }

    @Test
    public void testLoginUser_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(environment.getProperty(anyString())).thenReturn("User not found");

        // Act & Assert
        assertThrows(ValidateRecordException.class, () -> {
            authService.loginUser(loginRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testLoginUser_InvalidPassword() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        when(environment.getProperty(anyString())).thenReturn("Invalid credentials");

        // Act & Assert
        assertThrows(ValidateRecordException.class, () -> {
            authService.loginUser(loginRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testLoginUser_AccountSuspended() {
        // Arrange
        mockUser.setStatus(UserStatus.INACTIVE);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(environment.getProperty(anyString())).thenReturn("Account suspended");

        // Act & Assert
        assertThrows(ValidateRecordException.class, () -> {
            authService.loginUser(loginRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testLoginUser_EmailNotVerified() {
        // Arrange
        mockUser.setEmailVerified(YesNo.NO);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(environment.getProperty(anyString())).thenReturn("Email not verified");

        // Act & Assert
        assertThrows(ValidateRecordException.class, () -> {
            authService.loginUser(loginRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testRefreshToken_Success() {
        // Arrange
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("valid-refresh-token");

        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.isRefreshToken(anyString())).thenReturn(true);
        when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(jwtUtil.generateAccessToken(any(), anyString(), anyString(), anyString())).thenReturn("new-access-token");
        when(environment.getProperty(anyString())).thenReturn("Token refreshed successfully");

        // Act
        LoginResponse response = authService.refreshToken(refreshTokenRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("new-access-token", response.getAccessToken());
        
        verify(jwtUtil, times(1)).validateToken(anyString());
        verify(jwtUtil, times(1)).isRefreshToken(anyString());
    }

    @Test
    public void testResetPassword_Success() {
        // Arrange
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setEmail("test@example.com");
        resetPasswordRequest.setNewPassword("newPassword123");

        SuccessAndErrorDetailsResource mockResponse = new SuccessAndErrorDetailsResource();
        mockResponse.setMessages("Password reset successfully");

        when(verificationCodeService.verifyCode(any(VerifyCodeRequest.class))).thenReturn(mockResponse);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode(anyString())).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(environment.getProperty(anyString())).thenReturn("Password reset successfully");

        mockResponse.setIsValid(true);

        // Act
        SuccessAndErrorDetailsResource result = authService.resetPassword(resetPasswordRequest);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode("newPassword123");
    }
}
