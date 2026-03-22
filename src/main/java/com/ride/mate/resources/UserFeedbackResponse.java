package com.ride.mate.resources;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Builder
public class UserFeedbackResponse {
    private Long id;
    private Long userId;
    private String userFullName;
    private Integer rating;
    private String category;
    private String feedbackText;
    private String createdDate;
}
