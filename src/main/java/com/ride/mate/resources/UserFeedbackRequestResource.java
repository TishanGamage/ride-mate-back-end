package com.ride.mate.resources;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class UserFeedbackRequestResource {
    @NotNull(message = "userId is required")
    private Long userId;
    @NotNull(message = "rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;
    private String category;
    @NotBlank(message = "feedbackText is required")
    private String feedbackText;
}
