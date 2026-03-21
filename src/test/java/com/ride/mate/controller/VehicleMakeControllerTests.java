package com.ride.mate.controller;

import com.ride.mate.domain.VehicleMake;
import com.ride.mate.service.VehicleMakeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * VehicleMakeControllerTests
 * JUnit test cases for VehicleMakeController REST API endpoints
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
public class VehicleMakeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleMakeService vehicleMakeService;

    @Autowired
    private Environment environment;

    private VehicleMake mockVehicleMake;
    private List<VehicleMake> mockVehicleMakes;

    @Before
    public void setUp() {
        // Setup mock vehicle make
        mockVehicleMake = new VehicleMake();
        mockVehicleMake.setId(1L);
        mockVehicleMake.setName("Toyota");
        mockVehicleMake.setStatus("ACTIVE");

        VehicleMake vehicleMake2 = new VehicleMake();
        vehicleMake2.setId(2L);
        vehicleMake2.setName("Honda");
        vehicleMake2.setStatus("ACTIVE");

        mockVehicleMakes = Arrays.asList(mockVehicleMake, vehicleMake2);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"DRIVER"})
    public void testGetVehicleMakeById_Success() throws Exception {
        // Arrange
        when(vehicleMakeService.findById(1L)).thenReturn(Optional.of(mockVehicleMake));

        // Act & Assert
        mockMvc.perform(get("/vehicle-make/get-vehicle-make/id/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Toyota"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"DRIVER"})
    public void testGetVehicleMakeById_NotFound() throws Exception {
        // Arrange
        when(vehicleMakeService.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/vehicle-make/get-vehicle-make/id/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"DRIVER"})
    public void testGetVehicleMakeByStatus_Success() throws Exception {
        // Arrange
        when(vehicleMakeService.findByStatus("ACTIVE")).thenReturn(mockVehicleMakes);

        // Act & Assert
        mockMvc.perform(get("/vehicle-make/get-vehicle-make/status/ACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Toyota"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Honda"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"DRIVER"})
    public void testGetVehicleMakeByStatus_NotFound() throws Exception {
        // Arrange
        when(vehicleMakeService.findByStatus(anyString())).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/vehicle-make/get-vehicle-make/status/INACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
