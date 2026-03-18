package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.PaymentTransaction;
import com.ride.mate.domain.UserSavedCard;
import com.ride.mate.resources.*;
import com.ride.mate.service.PayHereService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PaymentController
 * REST API endpoints for PayHere payment gateway operations.
 * Handles the notify callback (public), passenger charge, and card/transaction queries.
 *
 * @author Danushka
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-03-2026    N/A          N/A          Danushka          Initial Development
 */
@Slf4j
@RestController
@RequestMapping(value = "/payment")
@CrossOrigin(origins = "*")
public class PaymentController extends MessagePropertyBase {

    private final PayHereService payHereService;
    private final Environment environment;

    public PaymentController(PayHereService payHereService, Environment environment) {
        this.payHereService = payHereService;
        this.environment = environment;
    }

    /**
     * PayHere Notify URL (PUBLIC endpoint — no JWT required)
     * PayHere POSTs application/x-www-form-urlencoded here after a successful card tokenization.
     * Must return HTTP 200 with body "OK" for PayHere to consider delivery successful.
     *
     * @param merchantId      PayHere merchant ID
     * @param orderId         Order ID sent from frontend
     * @param paymentId       PayHere payment ID
     * @param payhereAmount   Amount charged
     * @param payhereCurrency Currency code
     * @param statusCode      2 = success
     * @param md5sig          MD5 signature for verification
     * @param customerToken   Saved card token for recurring charges
     * @param method          Payment method (VISA, MASTER, etc.)
     * @param cardHolderName  Name on card
     * @param cardNo          Masked card number
     * @param cardExpiry      Card expiry date
     * @param custom1         User ID passed from frontend
     * @return "OK" string with HTTP 200
     */
    @PostMapping(value = "/notify")
    public ResponseEntity<String> payHereNotify(
            @RequestParam(value = "merchant_id", required = false) String merchantId,
            @RequestParam(value = "order_id", required = false) String orderId,
            @RequestParam(value = "payment_id", required = false) String paymentId,
            @RequestParam(value = "payhere_amount", required = false) String payhereAmount,
            @RequestParam(value = "payhere_currency", required = false) String payhereCurrency,
            @RequestParam(value = "status_code", required = false) String statusCode,
            @RequestParam(value = "md5sig", required = false) String md5sig,
            @RequestParam(value = "customer_token", required = false) String customerToken,
            @RequestParam(value = "method", required = false) String method,
            @RequestParam(value = "card_holder_name", required = false) String cardHolderName,
            @RequestParam(value = "card_no", required = false) String cardNo,
            @RequestParam(value = "card_expiry", required = false) String cardExpiry,
            @RequestParam(value = "custom_1", required = false) String custom1) {

        log.info("Received PayHere notify callback for order_id: {}", orderId);

        PayHereNotifyResource request = new PayHereNotifyResource();
        request.setMerchantId(merchantId);
        request.setOrderId(orderId);
        request.setPaymentId(paymentId);
        request.setPayhereAmount(payhereAmount);
        request.setPayhereCurrency(payhereCurrency);
        request.setStatusCode(statusCode);
        request.setMd5sig(md5sig);
        request.setCustomerToken(customerToken);
        request.setMethod(method);
        request.setCardHolderName(cardHolderName);
        request.setCardNo(cardNo);
        request.setCardExpiry(cardExpiry);
        request.setCustom1(custom1);

        payHereService.processNotifyCallback(request);
        return ResponseEntity.ok("OK");
    }

    /**
     * Charge a passenger using their saved card token via PayHere recurring API
     *
     * @param request payment init request with userId, rideDetailId, amount, currency
     * @return ResponseEntity with success details
     */
    @PostMapping(value = "/charge")
    public ResponseEntity<SuccessAndErrorDetailsResource> chargePassenger(
            @Valid @RequestBody PaymentInitResource request) {
        log.info("Received charge request for userId: {}", request.getUserId());

        PaymentTransaction transaction = payHereService.chargePassenger(request);

        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(transaction.getId());
        response.setMessages(environment.getProperty(RECORD_CREATED));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get all saved cards for a user
     *
     * @param userId the user's ID
     * @return list of saved card response resources
     */
    @GetMapping(value = "/saved-cards/{userId}")
    public ResponseEntity<List<SavedCardResponseResource>> getSavedCards(@PathVariable Long userId) {
        log.info("Fetching saved cards for userId: {}", userId);

        List<UserSavedCard> cards = payHereService.getSavedCards(userId);
        List<SavedCardResponseResource> response = cards.stream()
                .map(card -> SavedCardResponseResource.builder()
                        .id(card.getId())
                        .cardHolderName(card.getCardHolderName())
                        .cardNoMasked(card.getCardNoMasked())
                        .cardExpiry(card.getCardExpiry())
                        .paymentMethod(card.getPaymentMethod())
                        .isActive(card.getIsActive() != null ? card.getIsActive().name() : null)
                        .build())
                .collect(Collectors.toList());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get all payment transactions for a user
     *
     * @param userId the user's ID
     * @return list of payment transaction response resources
     */
    @GetMapping(value = "/transactions/{userId}")
    public ResponseEntity<List<PaymentTransactionResponseResource>> getTransactions(@PathVariable Long userId) {
        log.info("Fetching transactions for userId: {}", userId);

        List<PaymentTransaction> transactions = payHereService.getTransactions(userId);
        List<PaymentTransactionResponseResource> response = transactions.stream()
                .map(txn -> PaymentTransactionResponseResource.builder()
                        .id(txn.getId())
                        .orderId(txn.getOrderId())
                        .payhereAmount(txn.getPayhereAmount())
                        .currency(txn.getCurrency())
                        .status(txn.getStatus() != null ? txn.getStatus().name() : null)
                        .method(txn.getMethod())
                        .createdDate(txn.getCreatedDate())
                        .build())
                .collect(Collectors.toList());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

