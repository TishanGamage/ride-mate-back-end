package com.ride.mate.service;

import com.ride.mate.domain.PaymentTransaction;
import com.ride.mate.domain.UserSavedCard;
import com.ride.mate.resources.PayHereNotifyResource;
import com.ride.mate.resources.PaymentInitResource;

import java.util.List;

/**
 * PayHereService
 * Business logic interface for PayHere payment gateway operations
 *
 * @author Danushka
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-03-2026    N/A          N/A          Danushka          Initial Development
 */
public interface PayHereService {

    void processNotifyCallback(PayHereNotifyResource request);

    PaymentTransaction chargePassenger(PaymentInitResource request);

    List<UserSavedCard> getSavedCards(Long userId);

    List<PaymentTransaction> getTransactions(Long userId);
}

