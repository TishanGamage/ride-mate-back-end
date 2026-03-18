package com.ride.mate.service.impl;

import com.ride.mate.config.PayHereConfig;
import com.ride.mate.core.LoginAuthentication;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.*;
import com.ride.mate.enums.PaymentStatus;
import com.ride.mate.enums.YesNo;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.*;
import com.ride.mate.resources.PayHereNotifyResource;
import com.ride.mate.resources.PaymentInitResource;
import com.ride.mate.service.PayHereService;
import com.ride.mate.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

/**
 * PayHereServiceImpl
 * Service implementation for PayHere payment gateway operations.
 * Handles notify callback verification, card token saving, and recurring charges.
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
@Service
@Transactional
public class PayHereServiceImpl extends MessagePropertyBase implements PayHereService {

    private final UserSavedCardRepository userSavedCardRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final DriverEarningRepository driverEarningRepository;
    private final UserRepository userRepository;
    private final RideDetailRepository rideDetailRepository;
    private final PayHereConfig payHereConfig;
    private final RestTemplate restTemplate;
    private final Environment environment;


    public PayHereServiceImpl(UserSavedCardRepository userSavedCardRepository,
                              PaymentTransactionRepository paymentTransactionRepository,
                              DriverEarningRepository driverEarningRepository,
                              UserRepository userRepository,
                              RideDetailRepository rideDetailRepository,
                              PayHereConfig payHereConfig,
                              RestTemplate restTemplate,
                              Environment environment) {
        this.userSavedCardRepository = userSavedCardRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.driverEarningRepository = driverEarningRepository;
        this.userRepository = userRepository;
        this.rideDetailRepository = rideDetailRepository;
        this.payHereConfig = payHereConfig;
        this.restTemplate = restTemplate;
        this.environment = environment;
    }

    // ─── Notify Callback ────────────────────────────────────────────────────────

    @Override
    public void processNotifyCallback(PayHereNotifyResource request) {
        log.info("Processing PayHere notify callback for order_id: {}", request.getOrderId());

        // 1. Verify MD5 signature
        if (!isSignatureValid(request)) {
            log.warn("PayHere notify callback: invalid MD5 signature for order_id: {}", request.getOrderId());
            throw new ValidateRecordException(environment.getProperty(PAYMENT_SIGNATURE_INVALID), "message");
        }

        // 2. Only process successful payments (statusCode == "2")
        if (!"2".equals(request.getStatusCode())) {
            log.warn("PayHere notify: non-success status_code={} for order_id={}", request.getStatusCode(), request.getOrderId());
            return;
        }

        // 3. Extract userId from custom_1 field
        Long userId;
        try {
            userId = Long.parseLong(request.getCustom1());
        } catch (NumberFormatException e) {
            log.error("PayHere notify: invalid custom_1 userId value: {}", request.getCustom1());
            throw new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("PayHere notify: user not found for userId: {}", userId);
                    return new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message");
                });

        // 4. Save / update customer token
        if (request.getCustomerToken() != null && !request.getCustomerToken().isBlank()) {
            if (!userSavedCardRepository.existsByUserIdAndCustomerToken(userId, request.getCustomerToken())) {
                UserSavedCard savedCard = new UserSavedCard();
                savedCard.setUser(user);
                savedCard.setSyncTs(DateUtil.getDate());
                savedCard.setCustomerToken(request.getCustomerToken());
                savedCard.setCardHolderName(request.getCardHolderName());
                savedCard.setCardNoMasked(request.getCardNo());
                savedCard.setCardExpiry(request.getCardExpiry());
                savedCard.setPaymentMethod(request.getMethod());
                savedCard.setIsActive(YesNo.YES);
                savedCard.setCreatedDate(DateUtil.getDate());
                savedCard.setCreatedUser(LoginAuthentication.getUserName());
                userSavedCardRepository.save(savedCard);
                log.info("Saved new card token for userId: {}", userId);
            } else {
                log.info("Card token already exists for userId: {} — skipping duplicate save", userId);
            }
        }

        // 5. Record payment transaction (avoid duplicate order_id processing)
        if (!paymentTransactionRepository.existsByOrderId(request.getOrderId())) {
            PaymentTransaction txn = new PaymentTransaction();
            txn.setUser(user);
            txn.setSyncTs(DateUtil.getDate());
            txn.setOrderId(request.getOrderId());
            txn.setPaymentId(request.getPaymentId());
            txn.setPayhereAmount(new BigDecimal(request.getPayhereAmount()));
            txn.setCurrency(request.getPayhereCurrency() != null ? request.getPayhereCurrency() : "LKR");
            txn.setStatus(PaymentStatus.SUCCESS);
            txn.setCustomerToken(request.getCustomerToken());
            txn.setMethod(request.getMethod());
            txn.setCreatedDate(DateUtil.getDate());
            txn.setCreatedUser(LoginAuthentication.getUserName());
            paymentTransactionRepository.save(txn);
            log.info("Recorded payment transaction for order_id: {}", request.getOrderId());
        }

        log.info("PayHere notify callback processed successfully for order_id: {}", request.getOrderId());
    }

    // ─── Charge Passenger ───────────────────────────────────────────────────────

    @Override
    public PaymentTransaction chargePassenger(PaymentInitResource request) {
        log.info("Charging passenger userId: {} for rideDetailId: {}", request.getUserId(), request.getRideDetailId());

        // 1. Look up user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message"));

        // 2. Get active saved card
        UserSavedCard savedCard = userSavedCardRepository
                .findByUserIdAndIsActiveOrderByIdDesc(request.getUserId(), YesNo.YES)
                .orElseThrow(() -> {
                    log.warn("No active saved card for userId: {}", request.getUserId());
                    return new ValidateRecordException(environment.getProperty(SAVED_CARD_NOT_FOUND), "message");
                });

        // 3. Get ride detail
        RideDetail rideDetail = rideDetailRepository.findById(request.getRideDetailId())
                .orElseThrow(() -> new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message"));

        // 4. Generate unique order ID
        String orderId = generateOrderId();

        // 5. Call PayHere charge API
        boolean chargeSuccess = callPayHereChargeApi(
                savedCard.getCustomerToken(),
                orderId,
                request.getAmount(),
                request.getCurrency() != null ? request.getCurrency() : "LKR",
                request.getItemName() != null ? request.getItemName() : "RideMate Ride"
        );

        PaymentStatus status = chargeSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;

        // 6. Persist transaction
        PaymentTransaction txn = new PaymentTransaction();
        txn.setUser(user);
        txn.setRideDetail(rideDetail);
        txn.setOrderId(orderId);
        txn.setPayhereAmount(request.getAmount());
        txn.setCurrency(request.getCurrency() != null ? request.getCurrency() : "LKR");
        txn.setStatus(status);
        txn.setCustomerToken(savedCard.getCustomerToken());
        txn.setMethod(savedCard.getPaymentMethod());
        txn.setCreatedDate(DateUtil.getDate());
        txn.setCreatedUser(SYSTEM);
        PaymentTransaction saved = paymentTransactionRepository.save(txn);

        // 7. If successful, credit driver earnings
        if (chargeSuccess) {
            DriverProfile driverProfile = rideDetail.getDriverProfile();
            DriverEarning earning = new DriverEarning();
            earning.setDriverProfile(driverProfile);
            earning.setRideDetail(rideDetail);
            earning.setPassengerUser(user);
            earning.setAmount(request.getAmount());
            earning.setCurrency(request.getCurrency() != null ? request.getCurrency() : "LKR");
            earning.setStatus(PaymentStatus.PENDING);
            earning.setCreatedDate(DateUtil.getDate());
            earning.setCreatedUser(SYSTEM);
            driverEarningRepository.save(earning);
            log.info("Driver earning credited for driverProfileId: {}, amount: {}", driverProfile.getId(), request.getAmount());
        } else {
            log.warn("Charge failed for userId: {}, orderId: {}", request.getUserId(), orderId);
            throw new ValidateRecordException(environment.getProperty(PAYMENT_CHARGE_FAILED), "message");
        }

        log.info("Passenger charge completed with orderId: {}", orderId);
        return saved;
    }

    // ─── Query Methods ───────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<UserSavedCard> getSavedCards(Long userId) {
        log.info("Fetching saved cards for userId: {}", userId);
        return userSavedCardRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentTransaction> getTransactions(Long userId) {
        log.info("Fetching transactions for userId: {}", userId);
        return paymentTransactionRepository.findByUserId(userId);
    }

    // ─── Private Helpers ─────────────────────────────────────────────────────────

    /**
     * Verifies the MD5 signature from PayHere notify callback.
     * Formula: MD5(merchant_id + order_id + payhere_amount + payhere_currency + status_code + MD5(merchant_secret).toUpperCase())
     */
    private boolean isSignatureValid(PayHereNotifyResource request) {
        try {
            String merchantSecretHash = computeMd5(payHereConfig.getMerchantSecret()).toUpperCase();
            String rawSignature = payHereConfig.getMerchantId()
                    + request.getOrderId()
                    + request.getPayhereAmount()
                    + request.getPayhereCurrency()
                    + request.getStatusCode()
                    + merchantSecretHash;
            String expectedSig = computeMd5(rawSignature).toUpperCase();
            return expectedSig.equals(request.getMd5sig() != null ? request.getMd5sig().toUpperCase() : "");
        } catch (Exception e) {
            log.error("Error computing MD5 signature: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Calls PayHere recurring charge API to charge a passenger using their saved token.
     */
    private boolean callPayHereChargeApi(String customerToken, String orderId, BigDecimal amount, String currency, String itemName) {
        try {
            String url = payHereConfig.getApiBaseUrl() + payHereConfig.getApi().getChargePath();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("merchant_id", payHereConfig.getMerchantId());
            body.add("customer_token", customerToken);
            body.add("order_id", orderId);
            body.add("amount", amount.toPlainString());
            body.add("currency", currency);
            body.add("item_name", itemName);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            log.info("PayHere charge API response status: {}, body: {}", response.getStatusCode(), response.getBody());
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("PayHere charge API call failed: {}", e.getMessage());
            return false;
        }
    }

    private String generateOrderId() {
        return "RM-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private String computeMd5(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

