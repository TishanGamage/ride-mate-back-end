package com.ride.mate.resources;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Builder
public class UserReportResponse {
    private Long id;
    private Long userId;
    private String userFullName;
    private String category;
    private String subject;
    private String description;
    private String status;
    private String createdDate;
}
