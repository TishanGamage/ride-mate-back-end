package com.ride.mate.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * PayHereNotifyResource
 * Represents the POST payload sent by PayHere to the notify_url after a card tokenization.
 * PayHere posts application/x-www-form-urlencoded — bind with @ModelAttribute or @RequestParam.
 *
 * @author Danushka
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-03-2026    N/A          N/A          Danushka          Initial Development
 */
@Getter
@Setter
public class PayHereNotifyResource {

    @JsonProperty("merchant_id")
    private String merchantId;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("payment_id")
    private String paymentId;

    @JsonProperty("payhere_amount")
    private String payhereAmount;

    @JsonProperty("payhere_currency")
    private String payhereCurrency;

    @JsonProperty("status_code")
    private String statusCode;

    @JsonProperty("md5sig")
    private String md5sig;

    @JsonProperty("customer_token")
    private String customerToken;

    @JsonProperty("method")
    private String method;

    @JsonProperty("card_holder_name")
    private String cardHolderName;

    @JsonProperty("card_no")
    private String cardNo;

    @JsonProperty("card_expiry")
    private String cardExpiry;

    // User ID passed as part of the order_id or custom_1 field from frontend
    @JsonProperty("custom_1")
    private String custom1;
}

