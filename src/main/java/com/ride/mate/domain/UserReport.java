package com.ride.mate.domain;

import com.ride.mate.core.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * User Report Entity
 * Stores problem/complaint reports submitted by app users.
 *
 * @author RideMate
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 22-03-2026    N/A          N/A          RideMate         Initial Development
 */
@Getter
@Setter
@Entity
@Table(name = "user_report")
public class UserReport extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "subject", length = 255)
    private String subject;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** PENDING | IN_REVIEW | RESOLVED | CLOSED */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_date", nullable = false)
    private Timestamp createdDate;

    @Column(name = "created_user", nullable = false, length = 100)
    private String createdUser;

    @Column(name = "modified_date")
    private Timestamp modifiedDate;

    @Column(name = "modified_user", length = 100)
    private String modifiedUser;
}

