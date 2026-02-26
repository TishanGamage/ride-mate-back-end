package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.User;
import com.ride.mate.resources.SuccessAndErrorDetailsResource;
import com.ride.mate.resources.UserRegistrationAddResource;
import com.ride.mate.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * User Registration Controller
 * REST API endpoints for user registration
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 26-02-2026    N/A          N/A          Tishan          Initial Development
 */
@Slf4j
@RestController
@RequestMapping(value = "/user-registration")
@CrossOrigin(origins = "*")
public class UserRegistrationController extends MessagePropertyBase {

    private final UserService userService;
    private final Environment environment;

    public UserRegistrationController(UserService userService, Environment environment, Environment environment1) {
        this.userService = userService;
        this.environment = environment1;
    }

    /**
     * Register a new user
     * Creates a new user record in the User table
     *
     * @param request user registration request containing email, phone, password, and role
     * @return ResponseEntity with user registration response
     */
    @PostMapping
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationAddResource request) {
        log.info("Received user registration request for email: {}", request.getEmail());
        User user = userService.registerUser(request);
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(user.getId());
        response.setMessages(environment.getProperty(RECORD_CREATED));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
