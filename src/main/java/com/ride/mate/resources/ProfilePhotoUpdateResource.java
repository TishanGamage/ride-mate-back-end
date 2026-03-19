package com.ride.mate.resources;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * ProfilePhotoUpdateResource
 * Request payload for updating the profile photo of a user profile
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 19-03-2026    N/A          N/A          Tishan          Initial Development
 */
@Getter
@Setter
public class ProfilePhotoUpdateResource {

    @NotNull(message = "{invalid.value}")
    private Long profileImageDocumentId;

}

