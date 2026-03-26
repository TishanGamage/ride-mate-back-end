package com.ride.mate.resources;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminUserStatusRequest {

    @NotBlank(message = "{can.not.be.blank}")
    private String status; // ACTIVE | INACTIVE | PENDING
}
