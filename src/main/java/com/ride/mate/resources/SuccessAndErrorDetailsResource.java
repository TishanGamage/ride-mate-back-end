package com.ride.mate.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Success and Error Details Resource
 * Standard response object for API responses
 *
 * @author Tishan 
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-02-2026    N/A          N/A          Tishan          Initial Development
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuccessAndErrorDetailsResource {

    private String messages;
    private String details;
    private String code;
    private Boolean isValid = null;

    public SuccessAndErrorDetailsResource(String messages) {
        this.messages = messages;
    }

    public SuccessAndErrorDetailsResource(String messages, String details) {
        this.messages = messages;
        this.details = details;
    }

    public SuccessAndErrorDetailsResource(String messages, boolean isValid) {
        this.messages = messages;
        this.isValid = isValid;
    }


}

