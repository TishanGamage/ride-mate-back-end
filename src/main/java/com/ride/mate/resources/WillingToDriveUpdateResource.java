package com.ride.mate.resources;

import com.ride.mate.enums.YesNo;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * WillingToDriveUpdateResource
 * Request payload for updating the willingToDrive field of a user profile
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-03-2026    N/A          N/A          Tishan          Initial Development
 */
@Getter
@Setter
public class WillingToDriveUpdateResource {

    @NotNull(message = "{invalid.value}")
    private YesNo willingToDrive;

}

