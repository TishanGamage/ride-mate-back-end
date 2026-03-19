package com.ride.mate.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * SavedCardResponseResource
 * Response DTO for a user's saved payment card
 *
 * @author Danushka
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-03-2026    N/A          N/A          Danushka          Initial Development
 */
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SavedCardResponseResource {

    private Long id;
    private String cardHolderName;
    private String cardNoMasked;
    private String cardExpiry;
    private String paymentMethod;
    private String isActive;
}

