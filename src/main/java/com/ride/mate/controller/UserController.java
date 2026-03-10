package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.User;
import com.ride.mate.resources.SuccessAndErrorDetailsResource;
import com.ride.mate.resources.UserRegistrationUpdateResource;
import com.ride.mate.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * User Controller
 * REST API endpoints for user management operations
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 09-03-2026    N/A          N/A          Tishan          Initial Development
 */
@Slf4j
@RestController
@RequestMapping(value = "/user")
@CrossOrigin(origins = "*")
public class UserController extends MessagePropertyBase {

    private final UserService userService;
    private final Environment environment;

    public UserController(UserService userService, Environment environment) {
        this.userService = userService;
        this.environment = environment;
    }

    /**
     * Update user information
     * Updates existing user record with new information
     *
     * @param request user update request containing user details
     * @return ResponseEntity with user update response
     */
    @PutMapping
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserRegistrationUpdateResource request) {
        log.info("Received user update request for user ID: {}", request.getId());
        User user = userService.updateUser(request);
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(user.getId());
        response.setMessages(environment.getProperty(RECORD_UPDATED));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

