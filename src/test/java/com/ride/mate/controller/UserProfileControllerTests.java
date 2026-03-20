package com.ride.mate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ride.mate.domain.UserProfile;
import com.ride.mate.enums.YesNo;
import com.ride.mate.resources.*;
import com.ride.mate.service.UserProfileService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserProfileControllerTests
 * JUnit test cases for UserProfileController REST API endpoints
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
public class UserProfileControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserProfileService userProfileService;

    private UserProfileAddResource userProfileAddRequest;
    private UserProfileUpdateResource userProfileUpdateRequest;
    private UserProfile mockUserProfile;
    private UserProfileResponse mockUserProfileResponse;

    @Before
    public void setUp() {
        // Setup user profile add request
        userProfileAddRequest = new UserProfileAddResource();
        userProfileAddRequest.setUserId(1L);
        userProfileAddRequest.setDateOfBirth("1995-05-15");
        userProfileAddRequest.setGender("Male");
        userProfileAddRequest.setWillingToDrive(YesNo.YES);

        // Setup user profile update request
        userProfileUpdateRequest = new UserProfileUpdateResource();
        userProfileUpdateRequest.setDateOfBirth("1995-05-15");
        userProfileUpdateRequest.setGender("Male");
        userProfileUpdateRequest.setBio("Experienced driver");

        // Setup mock user profile
        mockUserProfile = new UserProfile();
        mockUserProfile.setId(1L);
        mockUserProfile.setGender("Male");
        mockUserProfile.setBio("Experienced driver");
        mockUserProfile.setWillingToDrive(YesNo.YES);
        mockUserProfile.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
        mockUserProfile.setCreatedUser("SYSTEM");

        // Setup mock user profile response using builder pattern
        mockUserProfileResponse = UserProfileResponse.builder()
                .id(1L)
                .userId(1L)
                .gender("Male")
                .bio("Experienced driver")
                .willingToDrive(YesNo.YES.name())
                .build();
    }

    @Test
    public void testCreateUserProfile_Success() throws Exception {
        // Arrange
        when(userProfileService.createUserProfile(any(UserProfileAddResource.class)))
                .thenReturn(mockUserProfile);

        // Act & Assert
        mockMvc.perform(post("/user-profile/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userProfileAddRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testCreateUserProfile_MissingUserId() throws Exception {
        // Arrange
        userProfileAddRequest.setUserId(null);

        // Act & Assert
        mockMvc.perform(post("/user-profile/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userProfileAddRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateUserProfile_Success() throws Exception {
        // Arrange
        when(userProfileService.updateUserProfile(any(UserProfileUpdateResource.class), eq(1L)))
                .thenReturn(mockUserProfile);

        // Act & Assert
        mockMvc.perform(put("/user-profile/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userProfileUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testGetUserProfileByUserId_Success() throws Exception {
        // Arrange
        when(userProfileService.getUserProfileByUserId(1L)).thenReturn(mockUserProfileResponse);

        // Act & Assert
        mockMvc.perform(get("/user-profile/user/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.gender").value("Male"))
                .andExpect(jsonPath("$.willingToDrive").value("YES"));
    }

    @Test
    public void testUpdateWillingToDrive_Success() throws Exception {
        // Arrange
        WillingToDriveUpdateResource request = new WillingToDriveUpdateResource();
        request.setWillingToDrive(YesNo.YES);

        when(userProfileService.updateWillingToDrive(eq(1L), any(WillingToDriveUpdateResource.class)))
                .thenReturn(mockUserProfile);

        // Act & Assert
        mockMvc.perform(patch("/user-profile/1/willing-to-drive")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testUpdateProfilePhoto_Success() throws Exception {
        // Arrange
        ProfilePhotoUpdateResource request = new ProfilePhotoUpdateResource();
        request.setProfileImageDocumentId(10L);

        when(userProfileService.updateProfilePhoto(eq(1L), any(ProfilePhotoUpdateResource.class)))
                .thenReturn(mockUserProfile);

        // Act & Assert
        mockMvc.perform(patch("/user-profile/1/profile-photo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testUpdateRole_Success() throws Exception {
        // Arrange
        UpdateRoleRequest request = new UpdateRoleRequest();
        request.setRole("DRIVER");

        SuccessAndErrorDetailsResource mockResponse = new SuccessAndErrorDetailsResource();
        mockResponse.setMessages("Role updated successfully");

        when(userProfileService.updateRole(eq(1L), any(UpdateRoleRequest.class)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(patch("/user-profile/1/update-role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages").value("Role updated successfully"));
    }
}
