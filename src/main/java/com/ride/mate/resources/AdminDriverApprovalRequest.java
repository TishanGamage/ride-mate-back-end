package com.ride.mate.resources;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminDriverApprovalRequest {

    @NotBlank(message = "{can.not.be.blank}")
    private String accountStatus; // APPROVED | REJECTED | SUSPENDED

    private String remarks;
}
