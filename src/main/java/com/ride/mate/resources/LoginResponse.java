package com.ride.mate.resources;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Login Response Resource
 * Response payload containing JWT tokens and user information
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 28-02-2026    N/A          N/A          Iruni          Initial Development
 * 2 09-03-2026    N/A          N/A          Tishan         Added JWT token fields
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {

    private String message;
    private boolean success;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private Long userId;
    private String userName;
    private String email;
    private String role;
    private String emailVerified;
}

