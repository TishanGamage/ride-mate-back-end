package com.ride.mate.controller;

import com.ride.mate.domain.IdentificationType;
import com.ride.mate.service.IdentificationTypeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
 * IdentificationTypeControllerTests
 * JUnit test cases for IdentificationTypeController REST API endpoints
 *
 * @author Tishan
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
public class IdentificationTypeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IdentificationTypeService identificationTypeService;

    private IdentificationType mockIdentificationType;
    private List<IdentificationType> mockIdentificationTypes;

    @Before
    public void setUp() {
        // Setup mock identification type
        mockIdentificationType = new IdentificationType();
        mockIdentificationType.setId(1L);
        mockIdentificationType.setName("NIC");
        mockIdentificationType.setDescription("National Identity Card");
        mockIdentificationType.setCode("NIC");
        mockIdentificationType.setStatus("ACTIVE");

        IdentificationType identificationType2 = new IdentificationType();
        identificationType2.setId(2L);
        identificationType2.setName("Passport");
        identificationType2.setDescription("International Passport");
        identificationType2.setCode("PASSPORT");
        identificationType2.setStatus("ACTIVE");

        mockIdentificationTypes = Arrays.asList(mockIdentificationType, identificationType2);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"DRIVER"})
    public void testGetIdentificationTypeById_Success() throws Exception {
        // Arrange
        when(identificationTypeService.findById(1L)).thenReturn(Optional.of(mockIdentificationType));

        // Act & Assert
        mockMvc.perform(get("/identification-type/get-identification-type/id/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("NIC"))
                .andExpect(jsonPath("$.code").value("NIC"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"DRIVER"})
    public void testGetIdentificationTypeById_NotFound() throws Exception {
        // Arrange
        when(identificationTypeService.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/identification-type/get-identification-type/id/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"DRIVER"})
    public void testGetIdentificationTypeByStatus_Success() throws Exception {
        // Arrange
        when(identificationTypeService.findByStatus("ACTIVE")).thenReturn(mockIdentificationTypes);

        // Act & Assert
        mockMvc.perform(get("/identification-type/get-identification-type/status/ACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("NIC"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Passport"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"DRIVER"})
    public void testGetIdentificationTypeByStatus_NotFound() throws Exception {
        // Arrange
        when(identificationTypeService.findByStatus(anyString())).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/identification-type/get-identification-type/status/INACTIVE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
