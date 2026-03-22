package com.ride.mate.resources;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class UserReportRequestResource {
    @NotNull(message = "userId is required")
    private Long userId;
    @NotBlank(message = "category is required")
    private String category;
    private String subject;
    @NotBlank(message = "description is required")
    private String description;
}
