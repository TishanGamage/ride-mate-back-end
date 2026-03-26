package com.ride.mate.resources;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminReportStatusRequest {

    @NotBlank(message = "{can.not.be.blank}")
    private String status; // PENDING | IN_REVIEW | RESOLVED | CLOSED
}
