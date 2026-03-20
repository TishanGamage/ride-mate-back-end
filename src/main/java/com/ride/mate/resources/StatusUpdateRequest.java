package com.ride.mate.resources;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Status Update Request (DTO)
 * Request payload for updating ride status
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-03-2026    N/A          N/A          Iruni           Initial Development
 */
@Getter
@Setter
public class StatusUpdateRequest {

    @NotBlank(message = "{can.not.be.blank}")
    private String status;
}
