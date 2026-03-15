package com.ride.mate.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

/**
 * Validate Resource
 * Resource class for validation error responses
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-02-2026    N/A          N/A          Tishan          Initial Development
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidateResource {

    @JsonProperty("email")
    private String email;

    @JsonProperty("code")
    private String code;

    @JsonProperty("verificationCode")
    private String verificationCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("errorMessage")
    private String errorMessage;
}
