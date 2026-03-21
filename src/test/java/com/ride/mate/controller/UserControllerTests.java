package com.ride.mate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ride.mate.domain.User;
import com.ride.mate.enums.UserRole;
import com.ride.mate.enums.UserStatus;
import com.ride.mate.enums.YesNo;
import com.ride.mate.resources.UserRegistrationUpdateResource;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserControllerTests
 * JUnit test cases for UserController REST API endpoints
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
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Autowired
    private Environment environment;

    private UserRegistrationUpdateResource updateRequest;
    private User mockUser;

    @Before
    public void setUp() {
        // Setup update request
        updateRequest = new UserRegistrationUpdateResource();
        updateRequest.setId(1L);
        updateRequest.setEmail("updated@example.com");
        updateRequest.setFirstName("John");
        updateRequest.setLastName("Doe");
        updateRequest.setPhoneNumber("1234567890");
        updateRequest.setPassword("newPassword123");
        updateRequest.setUserRole(UserRole.PASSENGER);
        updateRequest.setVersion("1");

        // Setup mock user
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("updated@example.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setPhoneNumber("1234567890");
        mockUser.setUserRole(UserRole.PASSENGER);
        mockUser.setStatus(UserStatus.ACTIVE);
        mockUser.setEmailVerified(YesNo.YES);
        mockUser.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
        mockUser.setCreatedUser("SYSTEM");
    }

    @Test
    public void testUpdateUser_Success() throws Exception {
        // Arrange
        when(userService.updateUser(any(UserRegistrationUpdateResource.class))).thenReturn(mockUser);

        // Act & Assert
        mockMvc.perform(put("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testUpdateUser_InvalidEmail() throws Exception {
        // Arrange
        updateRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(put("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateUser_InvalidPhoneNumber() throws Exception {
        // Arrange
        updateRequest.setPhoneNumber("123"); // Too short

        // Act & Assert
        mockMvc.perform(put("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }
}
