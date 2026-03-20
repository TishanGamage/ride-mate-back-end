package com.ride.mate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ride.mate.domain.PaymentTransaction;
import com.ride.mate.domain.UserSavedCard;
import com.ride.mate.resources.PaymentInitResource;
import com.ride.mate.service.PayHereService;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PaymentControllerTests
 * JUnit test cases for PaymentController REST API endpoints
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
public class PaymentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PayHereService payHereService;


    private PaymentInitResource chargeRequest;
    private PaymentTransaction mockPaymentTransaction;
    private List<UserSavedCard> mockSavedCards;

    @Before
    public void setUp() {
        // Setup charge request
        chargeRequest = new PaymentInitResource();
        chargeRequest.setUserId(1L);
        chargeRequest.setRideDetailId(1L);
        chargeRequest.setAmount(new BigDecimal("500.00"));
        chargeRequest.setCurrency("LKR");

        // Setup mock payment transaction
        mockPaymentTransaction = new PaymentTransaction();
        mockPaymentTransaction.setId(1L);
        mockPaymentTransaction.setOrderId("TXN_12345");
        mockPaymentTransaction.setPayhereAmount(new BigDecimal("500.00"));

        // Setup mock saved cards
        UserSavedCard card1 = new UserSavedCard();
        card1.setId(1L);
        card1.setCustomerToken("token_12345");
        card1.setCardNoMasked("************1234");
        card1.setPaymentMethod("VISA");
        card1.setCardHolderName("John Doe");

        mockSavedCards = Collections.singletonList(card1);
    }

    @Test
    public void testPayHereNotify_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/payment/notify")
                        .param("merchant_id", "1234567")
                        .param("order_id", "ORD_123")
                        .param("payment_id", "PAY_123")
                        .param("payhere_amount", "500.00")
                        .param("payhere_currency", "LKR")
                        .param("status_code", "2")
                        .param("md5sig", "signature")
                        .param("customer_token", "token_12345")
                        .param("method", "VISA")
                        .param("card_holder_name", "John Doe")
                        .param("card_no", "************1234")
                        .param("card_expiry", "12/25")
                        .param("custom_1", "1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));
    }

    @Test
    public void testChargePassenger_Success() throws Exception {
        // Arrange
        when(payHereService.chargePassenger(any(PaymentInitResource.class)))
                .thenReturn(mockPaymentTransaction);

        // Act & Assert
        mockMvc.perform(post("/payment/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chargeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.messages").exists());
    }

    @Test
    public void testChargePassenger_MissingFields() throws Exception {
        // Arrange
        PaymentInitResource invalidRequest = new PaymentInitResource();
        // Leave userId and amount null to trigger validation

        // Act & Assert
        mockMvc.perform(post("/payment/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetSavedCardsByUserId_Success() throws Exception {
        // Arrange
        when(payHereService.getSavedCards(1L)).thenReturn(mockSavedCards);

        // Act & Assert
        mockMvc.perform(get("/payment/saved-cards/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].cardHolderName").value("John Doe"))
                .andExpect(jsonPath("$[0].cardNoMasked").value("************1234"))
                .andExpect(jsonPath("$[0].paymentMethod").value("VISA"));
    }

    @Test
    public void testGetPaymentTransactionsByUserId_Success() throws Exception {
        // Arrange
        List<PaymentTransaction> mockTransactions = Collections.singletonList(mockPaymentTransaction);
        when(payHereService.getTransactions(1L)).thenReturn(mockTransactions);

        // Act & Assert
        mockMvc.perform(get("/payment/transactions/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].orderId").value("TXN_12345"))
                .andExpect(jsonPath("$[0].payhereAmount").value(500.00));
    }

    @Test
    public void testDeleteSavedCard_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/payment/saved-cards/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
