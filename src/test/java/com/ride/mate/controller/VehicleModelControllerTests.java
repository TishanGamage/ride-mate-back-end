package com.ride.mate.controller;

import com.ride.mate.domain.VehicleModel;
import com.ride.mate.service.VehicleModelService;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * VehicleModelControllerTests
 * JUnit test cases for VehicleModelController REST API endpoints
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
public class VehicleModelControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VehicleModelService vehicleModelService;

    @Autowired
    private Environment environment;

    private List<VehicleModel> mockVehicleModels;

    @Before
    public void setUp() {
        // Setup mock vehicle models
        VehicleModel model1 = new VehicleModel();
        model1.setId(1L);
        model1.setName("Camry");
        model1.setStatus("ACTIVE");

        VehicleModel model2 = new VehicleModel();
        model2.setId(2L);
        model2.setName("Corolla");
        model2.setStatus("ACTIVE");

        mockVehicleModels = Arrays.asList(model1, model2);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"DRIVER"})
    public void testGetVehicleModelsByMakeIdAndStatus_Success() throws Exception {
        // Arrange
        when(vehicleModelService.findByVehicleMakeIdAndStatus(1L, "ACTIVE"))
                .thenReturn(mockVehicleModels);

        // Act & Assert
        mockMvc.perform(get("/vehicle-model/get-vehicle-models/vehicle-make-id/1/status/ACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Camry"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Corolla"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"DRIVER"})
    public void testGetVehicleModelsByMakeIdAndStatus_NotFound() throws Exception {
        // Arrange
        when(vehicleModelService.findByVehicleMakeIdAndStatus(anyLong(), anyString()))
                .thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/vehicle-model/get-vehicle-models/vehicle-make-id/999/status/ACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
