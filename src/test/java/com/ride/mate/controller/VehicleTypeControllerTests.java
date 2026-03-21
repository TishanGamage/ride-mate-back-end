package com.ride.mate.controller;

import com.ride.mate.domain.VehicleType;
import com.ride.mate.service.VehicleTypeService;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * VehicleTypeControllerTests
 * JUnit test cases for VehicleTypeController REST API endpoints
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
public class VehicleTypeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleTypeService vehicleTypeService;

    @Autowired
    private Environment environment;

    private VehicleType mockVehicleType;
    private List<VehicleType> mockVehicleTypes;

    @Before
    public void setUp() {
        // Setup mock vehicle type
        mockVehicleType = new VehicleType();
        mockVehicleType.setId(1L);
        mockVehicleType.setName("Sedan");
        mockVehicleType.setDescription("4-door sedan vehicle");
        mockVehicleType.setPerKmRate(new BigDecimal("50.00"));
        mockVehicleType.setStatus("ACTIVE");

        VehicleType vehicleType2 = new VehicleType();
        vehicleType2.setId(2L);
        vehicleType2.setName("SUV");
        vehicleType2.setDescription("Sport utility vehicle");
        vehicleType2.setPerKmRate(new BigDecimal("75.00"));
        vehicleType2.setStatus("ACTIVE");

        mockVehicleTypes = Arrays.asList(mockVehicleType, vehicleType2);
    }

    @Test
    public void testGetVehicleTypeById_Success() throws Exception {
        // Arrange
        when(vehicleTypeService.findById(1L)).thenReturn(Optional.of(mockVehicleType));

        // Act & Assert
        mockMvc.perform(get("/vehicle-type/get-vehicle-type/id/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Sedan"))
                .andExpect(jsonPath("$.perKmRate").value(50.00))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    public void testGetVehicleTypeById_NotFound() throws Exception {
        // Arrange
        when(vehicleTypeService.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/vehicle-type/get-vehicle-type/id/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetVehicleTypeByStatus_Success() throws Exception {
        // Arrange
        when(vehicleTypeService.findByStatus("ACTIVE")).thenReturn(mockVehicleTypes);

        // Act & Assert
        mockMvc.perform(get("/vehicle-type/get-vehicle-type/status/ACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Sedan"))
                .andExpect(jsonPath("$[0].perKmRate").value(50.00))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("SUV"))
                .andExpect(jsonPath("$[1].perKmRate").value(75.00));
    }

    @Test
    public void testGetVehicleTypeByStatus_NotFound() throws Exception {
        // Arrange
        when(vehicleTypeService.findByStatus(anyString())).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/vehicle-type/get-vehicle-type/status/INACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
