package com.ride.mate.domain;

import com.ride.mate.core.BaseEntity;
import com.ride.mate.enums.YesNo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Verification Code Entity
 * Stores email verification codes for authentication
 *
 * @author Tishan 
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-02-2026    N/A          N/A          Tishan          Initial Development
 */
@Getter
@Setter
@Entity
@Table(name = "verification_codes")
public class VerificationCode extends BaseEntity implements Serializable {

    @Column(name = "email")
    private String email;

    @Column(name = "code")
    private String code;

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "verified")
    private YesNo verified;

    @Column(name = "attempt_count")
    private Long attemptCount;

    @Column(name = "created_user")
    private String createdUser;

    @Column(name = "created_date")
    private Timestamp createdDate;

    @Column(name = "modified_user")
    private String modifiedUser;

    @Column(name = "modified_date")
    private Timestamp modifiedDate;
}

