package com.ride.mate.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Admin Payment Response
 * Response payload for payment transaction details in admin panel
 *
 * @author RideMate
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 02-04-2026    N/A          N/A          RideMate         Initial Development
 */
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminPaymentResponse {

    private Long id;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private Long rideDetailId;
    private String orderId;
    private String paymentId;
    private String amount;
    private String currency;
    private String status;
    private String method;
    private String createdDate;
}

