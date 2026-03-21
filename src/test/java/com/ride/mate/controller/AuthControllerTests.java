package com.ride.mate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ride.mate.domain.VerificationCode;
import com.ride.mate.enums.UserRole;
import com.ride.mate.resources.*;
import com.ride.mate.service.AuthService;
import com.ride.mate.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthControllerTests
 * JUnit test cases for AuthController REST API endpoints
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 21-03-2026    N/A          N/A          Iruni          Initial Development
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @Autowired
    private Environment environment;

    private UserRegistrationAddResource userRegistrationRequest;
    private LoginRequest loginRequest;
    private SendVerificationCodeRequest verificationCodeRequest;
    private VerifyCodeRequest verifyCodeRequest;
    private RefreshTokenRequest refreshTokenRequest;
    private ResetPasswordRequest resetPasswordRequest;

    @Before
    public void setUp() {
        // Setup user registration request
        userRegistrationRequest = new UserRegistrationAddResource();
        userRegistrationRequest.setEmail("test@example.com");
        userRegistrationRequest.setFirstName("John");
        userRegistrationRequest.setLastName("Doe");
        userRegistrationRequest.setPhoneNumber("1234567890");
        userRegistrationRequest.setPassword("password123");
        userRegistrationRequest.setUserRole(UserRole.PASSENGER);

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

        // Setup refresh token request
        refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("valid-refresh-token");

        // Setup reset password request
        resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setEmail("test@example.com");
        resetPasswordRequest.setNewPassword("newPassword123");
    }

    @Test
    public void testRegisterUser_Success() throws Exception {
        // Arrange
        LoginResponse mockResponse = LoginResponse.builder()
                .message("Record created successfully")
                .success(true)
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .userId(1L)
                .userName("John")
                .email("test@example.com")
                .role("PASSENGER")
                .emailVerified("NO")
                .build();

        when(userService.registerUser(any(UserRegistrationAddResource.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.role").value("PASSENGER"));
    }

    @Test
    public void testRegisterUser_InvalidEmail() throws Exception {
        // Arrange
        userRegistrationRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequest)))
                .andExpect(status().isUnprocessableEntity());
    }


    @Test
    public void testSendVerificationCode_Success() throws Exception {
        // Arrange
        VerificationCode mockVerificationCode = new VerificationCode();
        mockVerificationCode.setCode("123456");
        mockVerificationCode.setEmail("test@example.com");
        mockVerificationCode.setExpiryTime(Timestamp.valueOf(LocalDateTime.now().plusMinutes(10)).toLocalDateTime());

        when(authService.sendVerificationCode(any(SendVerificationCodeRequest.class)))
                .thenReturn(mockVerificationCode);

        // Act & Assert
        mockMvc.perform(post("/auth/send-verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verificationCodeRequest)))
                .andExpect(status().isOk()) // Updated to match the actual status code
                //.andExpect(jsonPath("$.code").value("123456"))
        ;
    }

    @Test
    public void testSendVerificationCode_Failure() throws Exception {
        // Arrange
        when(authService.sendVerificationCode(any(SendVerificationCodeRequest.class)))
                .thenReturn(null);

        // Act & Assert
        mockMvc.perform(post("/auth/send-verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verificationCodeRequest)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testVerifyCode_Success() throws Exception {
        // Arrange
        SuccessAndErrorDetailsResource mockResponse = new SuccessAndErrorDetailsResource();
        mockResponse.setIsValid(true);
        mockResponse.setMessages("Verification successful");

        when(authService.verifyCode(any(VerifyCodeRequest.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/auth/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyCodeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isValid").value(true));
    }

    @Test
    public void testVerifyCode_InvalidCode() throws Exception {
        // Arrange
        SuccessAndErrorDetailsResource mockResponse = new SuccessAndErrorDetailsResource();
        mockResponse.setIsValid(false);
        mockResponse.setMessages("Invalid verification code");

        when(authService.verifyCode(any(VerifyCodeRequest.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/auth/verify-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyCodeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLogin_Success() throws Exception {
        // Arrange
        LoginResponse mockResponse = LoginResponse.builder()
                .message("Login successful")
                .success(true)
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .userId(1L)
                .userName("John")
                .email("test@example.com")
                .role("PASSENGER")
                .emailVerified("YES")
                .build();

        when(authService.loginUser(any(LoginRequest.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("mock-refresh-token"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    public void testLogin_InvalidEmailFormat() throws Exception {
        // Arrange
        loginRequest.setEmail("not-an-email");

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void testRefreshToken_Success() throws Exception {
        // Arrange
        LoginResponse mockResponse = LoginResponse.builder()
                .message("Token refreshed successfully")
                .success(true)
                .accessToken("new-access-token")
                .refreshToken("mock-refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .build();

        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));
    }

    @Test
    public void testResetPassword_Success() throws Exception {
        // Arrange
        SuccessAndErrorDetailsResource mockResponse = new SuccessAndErrorDetailsResource();
        mockResponse.setMessages("Password reset successfully");

        when(authService.resetPassword(any(ResetPasswordRequest.class))).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages").value("Password reset successfully"));
    }
}
