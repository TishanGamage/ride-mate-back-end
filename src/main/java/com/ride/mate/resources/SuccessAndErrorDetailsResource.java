package com.ride.mate.resources;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuccessAndErrorDetailsResource {

    private String messages;
    private String details;
    private String code;

    public SuccessAndErrorDetailsResource(String messages) {
        this.messages = messages;
    }

    public SuccessAndErrorDetailsResource(String messages, String details) {
        this.messages = messages;
        this.details = details;
    }
}

