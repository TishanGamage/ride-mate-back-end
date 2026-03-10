package com.ride.mate.resources;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Refresh Token Request Resource
 * Request payload for refreshing access token
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 09-03-2026    N/A          N/A          Tishan          Initial Development
 */
@Getter
@Setter
public class RefreshTokenRequest {

    @NotBlank(message = "{can.not.be.blank}")
    private String refreshToken;
}

