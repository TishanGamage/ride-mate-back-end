package com.ride.mate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ride.mate.domain.DriverProfile;
import com.ride.mate.resources.DriverProfileRequestResource;
import com.ride.mate.resources.DriverProfileResponse;
import com.ride.mate.resources.DriverVehicleDetailsRequestResource;
import com.ride.mate.service.DriverProfileService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DriverProfileControllerTests
 * JUnit test cases for DriverProfileController REST API endpoints
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
public class DriverProfileControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DriverProfileService driverProfileService;

    @Autowired
    private Environment environment;

    private DriverProfileRequestResource driverProfileRequest;
    private DriverProfile mockDriverProfile;
    private DriverProfileResponse mockDriverProfileResponse;

    @Before
    public void setUp() {
        // Setup driver profile request
        driverProfileRequest = new DriverProfileRequestResource();
        driverProfileRequest.setDriverLicenseNumber("DL123456789");
        driverProfileRequest.setDriverLicenseExpiry("2030-12-31");

        // Setup vehicle details
        DriverVehicleDetailsRequestResource vehicleDetails = new DriverVehicleDetailsRequestResource();
        vehicleDetails.setVehicleMakeId(1L);
        vehicleDetails.setVehicleModelId(1L);
        vehicleDetails.setVehicleTypeId(1L);
        vehicleDetails.setRegistrationNumber("ABC-1234");
        vehicleDetails.setYear(2020);
        vehicleDetails.setColor("Black");
        vehicleDetails.setSeats(4);
        driverProfileRequest.setVehicleDetails(vehicleDetails);

        // Setup mock driver profile
        mockDriverProfile = new DriverProfile();
        mockDriverProfile.setId(1L);
        mockDriverProfile.setDriverLicenseNumber("DL123456789");
        mockDriverProfile.setDriverLicenseExpiry(LocalDate.parse("2030-12-31"));
        mockDriverProfile.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
        mockDriverProfile.setCreatedUser("SYSTEM");

        // Setup mock driver profile response
        mockDriverProfileResponse = DriverProfileResponse.builder()
                .id(1L)
                .userId(1L)
                .driverLicenseNumber("DL123456789")
                .driverLicenseExpiry("2030-12-31")
                .vehicles(new ArrayList<>())
                .build();
    }

    @Test
    public void testSaveDriverProfile_Success() throws Exception {
        // Arrange
        when(driverProfileService.saveDriverProfile(eq(1L), any(DriverProfileRequestResource.class)))
                .thenReturn(mockDriverProfile);

        // Act & Assert
        mockMvc.perform(post("/driver-profile/save/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(driverProfileRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testSaveDriverProfile_InvalidData() throws Exception {
        // Arrange - Invalid license number (blank)
        driverProfileRequest.setDriverLicenseNumber("");

        // Act & Assert
        mockMvc.perform(post("/driver-profile/save/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(driverProfileRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetDriverProfileByUserId_Success() throws Exception {
        // Arrange
        when(driverProfileService.getDriverProfileByUserId(1L)).thenReturn(mockDriverProfileResponse);

        // Act & Assert
        mockMvc.perform(get("/driver-profile/get-driver-profile/user/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.driverLicenseNumber").value("DL123456789"));
    }
}

