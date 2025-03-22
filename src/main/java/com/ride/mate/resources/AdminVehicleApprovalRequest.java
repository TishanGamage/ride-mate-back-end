package com.ride.mate.resources;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminVehicleApprovalRequest {

    @NotBlank(message = "{can.not.be.blank}")
    private String status; // APPROVED | REJECTED

    private String rejectionReason;
}
