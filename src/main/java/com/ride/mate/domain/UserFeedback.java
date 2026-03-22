package com.ride.mate.domain;

import com.ride.mate.core.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * User Feedback Entity
 * Stores star-rated feedback submitted by app users.
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
@Table(name = "user_feedback")
public class UserFeedback extends BaseEntity implements Serializable {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 1–5 star rating */
    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "feedback_text", columnDefinition = "TEXT")
    private String feedbackText;

    @Column(name = "created_date", nullable = false)
    private Timestamp createdDate;

    @Column(name = "created_user", nullable = false, length = 100)
    private String createdUser;
}

