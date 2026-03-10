package com.ride.mate.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRegistrationUpdateResource extends UserRegistrationAddResource {

    private Long id;

    private String version;

    @Valid
    private UserIdentificationDetailsRequestResource userIdentificationDetails;

}
