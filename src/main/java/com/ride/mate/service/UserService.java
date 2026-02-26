package com.ride.mate.service;

import com.ride.mate.domain.User;
import com.ride.mate.resources.UserRegistrationAddResource;
import com.ride.mate.resources.UserRegistrationResponse;

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
 */
public interface UserService {

    /**
     * Register a new user in the system
     * Creates a user record in the User table only
     *
     * @param request user registration request
     * @return user registration response
     */
    User registerUser(UserRegistrationAddResource request);
}

