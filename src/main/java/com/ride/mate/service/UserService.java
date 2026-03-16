package com.ride.mate.service;

import com.ride.mate.domain.User;
import com.ride.mate.resources.LoginResponse;
import com.ride.mate.resources.UserRegistrationAddResource;
import com.ride.mate.resources.UserRegistrationUpdateResource;

/**
 * User Service Interface
 * Business logic for user management operations
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 26-02-2026    N/A          N/A          Tishan          Initial Development
 * 2 09-03-2026    N/A          N/A          Tishan          Moved loginUser to AuthService
 * 3 16-03-2026    N/A          N/A          Tishan          Changed registerUser to return LoginResponse with tokens
 */
public interface UserService {

    /**
     * Register a new user in the system
     * Creates a user record and returns JWT tokens in response
     *
     * @param request user registration request
     * @return LoginResponse containing user info and JWT tokens
     */
    LoginResponse registerUser(UserRegistrationAddResource request);

    /**
     * Update an existing user in the system
     *
     * @param request user update request
     * @return updated user entity
     */
    User updateUser(UserRegistrationUpdateResource request);
}

