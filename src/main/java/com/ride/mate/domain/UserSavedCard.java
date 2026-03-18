package com.ride.mate.domain;

import com.ride.mate.core.BaseEntity;
import com.ride.mate.enums.YesNo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * UserSavedCard Entity
 * Stores PayHere tokenized card information for a user to enable recurring payments
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
@Entity
@Table(name = "user_saved_card")
public class UserSavedCard extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "customer_token", nullable = false, length = 500)
    private String customerToken;

    @Column(name = "card_holder_name", length = 200)
    private String cardHolderName;

    @Column(name = "card_no_masked", length = 20)
    private String cardNoMasked;

    @Column(name = "card_expiry", length = 10)
    private String cardExpiry;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_active", nullable = false, length = 3)
    private YesNo isActive = YesNo.YES;

    @Column(name = "created_date", nullable = false)
    private Timestamp createdDate;

    @Column(name = "created_user", nullable = false, length = 100)
    private String createdUser;

    @Column(name = "modified_date")
    private Timestamp modifiedDate;

    @Column(name = "modified_user", length = 100)
    private String modifiedUser;
}

