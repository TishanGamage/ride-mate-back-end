package com.ride.mate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ride.mate.resources.AvailableRideResponse;
import com.ride.mate.resources.RideRequestResource;
import com.ride.mate.resources.RideRequestResponse;
import com.ride.mate.service.RideRequestService;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RideRequestControllerTests
 * JUnit test cases for RideRequestController REST API endpoints
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
public class RideRequestControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RideRequestService rideRequestService;

    private RideRequestResource rideRequestResource;
    private RideRequestResponse mockRideRequestResponse;
    private List<AvailableRideResponse> mockAvailableRides;

    @Before
    public void setUp() {
        // Setup ride request resource
        rideRequestResource = new RideRequestResource();
        rideRequestResource.setRideDetailId(1L);
        rideRequestResource.setUserId(1L);
        rideRequestResource.setPassengerStartLat(new BigDecimal("6.9271"));
        rideRequestResource.setPassengerStartLng(new BigDecimal("79.8612"));
        rideRequestResource.setPassengerEndLat(new BigDecimal("6.8300"));
        rideRequestResource.setPassengerEndLng(new BigDecimal("79.9200"));
        rideRequestResource.setPassengerRideDistance(new BigDecimal("15.5"));
        rideRequestResource.setStartCity("Colombo");
        rideRequestResource.setEndCity("Moratuwa");

        // Setup mock ride request response
        mockRideRequestResponse = RideRequestResponse.builder()
                .id(1L)
                .rideDetailId(1L)
                .userId(1L)
                .passengerFirstName("John")
                .passengerLastName("Doe")
                .passengerEmail("test@example.com")
                .passengerPhone("1234567890")
                .passengerStartLat(new BigDecimal("6.9271"))
                .passengerStartLng(new BigDecimal("79.8612"))
                .passengerEndLat(new BigDecimal("6.8300"))
                .passengerEndLng(new BigDecimal("79.9200"))
                .startCity("Colombo")
                .endCity("Moratuwa")
                .passengerRideDistance(new BigDecimal("15.5"))
                .status("PENDING")
                .createdDate("2026-03-21T10:00:00")
                .build();

        // Setup mock available rides
        AvailableRideResponse availableRide = AvailableRideResponse.builder()
                .rideDetailId(1L)
                .driverFirstName("Jane")
                .driverLastName("Smith")
                .vehicleTypeName("Sedan")
                .startLat(new BigDecimal("6.9271"))
                .startLng(new BigDecimal("79.8612"))
                .endLat(new BigDecimal("6.8300"))
                .endLng(new BigDecimal("79.9200"))
                .startCity("Colombo")
                .endCity("Moratuwa")
                .availableSeats(3L)
                .totalRideCost(new BigDecimal("500.00"))
                .build();
        mockAvailableRides = Arrays.asList(availableRide);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"DRIVER"})
    public void testGetAvailableRides_Success() throws Exception {
        // Arrange
        when(rideRequestService.getAvailableRides(any(), any(), any())).thenReturn(mockAvailableRides);

        // Act & Assert
        mockMvc.perform(get("/ride-requests/available-rides")
                        .param("endLat", "6.8300")
                        .param("endLng", "79.9200")
                        .param("radiusKm", "15")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rideDetailId").value(1))
                .andExpect(jsonPath("$[0].driverFirstName").value("Jane"))
                .andExpect(jsonPath("$[0].availableSeats").value(3));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"DRIVER"})
    public void testGetAvailableRides_NoFilters() throws Exception {
        // Arrange
        when(rideRequestService.getAvailableRides(null, null, null)).thenReturn(mockAvailableRides);

        // Act & Assert
        mockMvc.perform(get("/ride-requests/available-rides")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"DRIVER"})
    public void testCreateRideRequest_Success() throws Exception {
        // Arrange
        when(rideRequestService.createRideRequest(any(RideRequestResource.class)))
                .thenReturn(mockRideRequestResponse);

        // Act & Assert
        mockMvc.perform(post("/ride-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rideRequestResource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rideDetailId").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"DRIVER"})
    public void testCreateRideRequest_MissingFields() throws Exception {
        // Arrange - Missing required fields
        rideRequestResource.setRideDetailId(null);
        rideRequestResource.setUserId(null);

        // Act & Assert
        mockMvc.perform(post("/ride-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rideRequestResource)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"DRIVER"})
    public void testGetPendingRequestsForDriver_Success() throws Exception {
        // Arrange
        List<RideRequestResponse> mockPendingRequests = Arrays.asList(mockRideRequestResponse);
        when(rideRequestService.getPendingRequestsForDriver(1L)).thenReturn(mockPendingRequests);

        // Act & Assert
        mockMvc.perform(get("/ride-requests/driver/1/pending")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"DRIVER"})
    public void testAcceptRideRequest_Success() throws Exception {
        // Arrange
        mockRideRequestResponse.setStatus("ACCEPTED");
        when(rideRequestService.acceptRideRequest(1L)).thenReturn(mockRideRequestResponse);

        // Act & Assert
        mockMvc.perform(put("/ride-requests/1/accept")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"DRIVER"})
    public void testRejectRideRequest_Success() throws Exception {
        // Arrange
        mockRideRequestResponse.setStatus("REJECTED");
        when(rideRequestService.rejectRideRequest(1L)).thenReturn(mockRideRequestResponse);

        // Act & Assert
        mockMvc.perform(put("/ride-requests/1/reject")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }
}

