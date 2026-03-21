package com.ride.mate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ride.mate.domain.RideDetail;
import com.ride.mate.resources.*;
import com.ride.mate.service.CostSplitService;
import com.ride.mate.service.RideDetailService;
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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RideDetailControllerTests
 * JUnit test cases for RideDetailController REST API endpoints
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
public class RideDetailControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RideDetailService rideDetailService;

    @MockBean
    private CostSplitService costSplitService;

    @Autowired
    private Environment environment;

    private RideDetailRequestResource rideDetailRequest;
    private RideDetail mockRideDetail;
    private RidePriceCalculationResponse mockPriceResponse;

    @Before
    public void setUp() {
        // Setup ride detail request
        rideDetailRequest = new RideDetailRequestResource();
        rideDetailRequest.setDriverProfileId(1L);
        rideDetailRequest.setStartLocationLatitude(new BigDecimal("6.9271"));
        rideDetailRequest.setStartLocationLongitude(new BigDecimal("79.8612"));
        rideDetailRequest.setEndLocationLatitude(new BigDecimal("6.8300"));
        rideDetailRequest.setEndLocationLongitude(new BigDecimal("79.9200"));
        rideDetailRequest.setStartCity("Colombo");
        rideDetailRequest.setEndCity("Moratuwa");
        rideDetailRequest.setTotalRideDistance(new BigDecimal("15.5"));
        rideDetailRequest.setPerKmRate(new BigDecimal("50.00"));
        rideDetailRequest.setTotalRideCost(new BigDecimal("775.00"));
        rideDetailRequest.setAvailableSeats(3L);
        rideDetailRequest.setStartTime("2026-03-21T10:00:00");
        rideDetailRequest.setStatus("ACTIVE");

        // Setup mock ride detail
        mockRideDetail = new RideDetail();
        mockRideDetail.setId(1L);
        mockRideDetail.setStartLocationLatitude(new BigDecimal("6.9271"));
        mockRideDetail.setStartLocationLongitude(new BigDecimal("79.8612"));
        mockRideDetail.setEndLocationLatitude(new BigDecimal("6.8300"));
        mockRideDetail.setEndLocationLongitude(new BigDecimal("79.9200"));
        mockRideDetail.setStartCity("Colombo");
        mockRideDetail.setEndCity("Moratuwa");
        mockRideDetail.setTotalRideDistance(new BigDecimal("15.5"));
        mockRideDetail.setPerKmRate(new BigDecimal("50.00"));
        mockRideDetail.setTotalRideCost(new BigDecimal("775.00"));
        mockRideDetail.setAvailableSeats(3L);
        mockRideDetail.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
        mockRideDetail.setCreatedUser("SYSTEM");

        // Setup mock price response
        mockPriceResponse = RidePriceCalculationResponse.builder()
                .totalRidePrice(new BigDecimal("775.00"))
                .perKmRate(new BigDecimal("50.00"))
                .totalDistance(new BigDecimal("15.5"))
                .vehicleTypeName("Sedan")
                .build();
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"DRIVER"})
    public void testCreateRideDetail_Success() throws Exception {
        // Arrange
        when(rideDetailService.createRideDetail(any(RideDetailRequestResource.class)))
                .thenReturn(mockRideDetail);

        // Act & Assert
        mockMvc.perform(post("/ride-details/addRide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rideDetailRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"DRIVER"})
    public void testCreateRideDetail_MissingFields() throws Exception {
        // Arrange - Missing required fields
        rideDetailRequest.setDriverProfileId(null);
        rideDetailRequest.setStartLocationLatitude(null);

        // Act & Assert
        mockMvc.perform(post("/ride-details/addRide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rideDetailRequest)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"DRIVER"})
    public void testCalculateRidePrice_Success() throws Exception {
        // Arrange
        when(rideDetailService.calculateRidePrice(eq(1L), any(BigDecimal.class)))
                .thenReturn(mockPriceResponse);

        // Act & Assert
        mockMvc.perform(get("/ride-details/calculate-price")
                        .param("driverProfileId", "1")
                        .param("totalDistance", "15.5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRidePrice").value(775.00))
                .andExpect(jsonPath("$.perKmRate").value(50.00))
                .andExpect(jsonPath("$.totalDistance").value(15.5))
                .andExpect(jsonPath("$.vehicleTypeName").value("Sedan"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"DRIVER"})
    public void testGetDriverRides_Success() throws Exception {
        // Arrange
        RideDetailResponseResource mockResponse = new RideDetailResponseResource();
        mockResponse.setId(1L);
        mockResponse.setStartCity("Colombo");
        mockResponse.setEndCity("Moratuwa");
        mockResponse.setTotalRideDistance(new BigDecimal("15.5"));
        mockResponse.setPerKmRate(new BigDecimal("50.00"));
        mockResponse.setAvailableSeats(3L);

        List<RideDetailResponseResource> mockRides = List.of(mockResponse);
        when(rideDetailService.getRidesByDriverProfileId(1L, null)).thenReturn(mockRides);

        // Act & Assert
        mockMvc.perform(get("/ride-details/driver/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].startCity").value("Colombo"))
                .andExpect(jsonPath("$[0].endCity").value("Moratuwa"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"DRIVER"})
    public void testGetCostSplitForRide_Success() throws Exception {
        // Arrange
        CostSplitResponse mockResponse = new CostSplitResponse();
        mockResponse.setRideDetailId(1L);
        mockResponse.setTotalRideCost(new BigDecimal("1500.00"));
        mockResponse.setDriverEffectiveCost(new BigDecimal("750.00"));
        mockResponse.setTotalPassengers(2);

        when(costSplitService.getCostSplit(1L)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/ride-details/1/cost-split")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideDetailId").value(1))
                .andExpect(jsonPath("$.totalRideCost").value(1500.00))
                .andExpect(jsonPath("$.driverEffectiveCost").value(750.00))
                .andExpect(jsonPath("$.totalPassengers").value(2));
    }
}
