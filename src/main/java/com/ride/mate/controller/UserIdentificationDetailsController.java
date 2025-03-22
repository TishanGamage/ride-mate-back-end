package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.UserIdentificationDetails;
import com.ride.mate.resources.SuccessAndErrorDetailsResource;
import com.ride.mate.resources.UserIdentificationDetailsResponseResource;
import com.ride.mate.service.UserIdentificationDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * User Identification Details Controller
 * REST API endpoints for user identification details
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 19-03-2026    N/A          N/A          Dulan          Initial Development
 * 2 22-03-2026    N/A          N/A          Dulan          Fixed getByUserId to return proper DTOs
 */
@Slf4j
@RestController
@RequestMapping(value = "/user-identification-details")
@CrossOrigin(origins = "*")
public class UserIdentificationDetailsController extends MessagePropertyBase {

    private final UserIdentificationDetailsService userIdentificationDetailsService;

    public UserIdentificationDetailsController(UserIdentificationDetailsService userIdentificationDetailsService) {
        this.userIdentificationDetailsService = userIdentificationDetailsService;
    }

    @GetMapping(value = "/get-by-id/{id}")
    public ResponseEntity<Object> getById(@PathVariable Long id) {
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        Optional<UserIdentificationDetails> details = userIdentificationDetailsService.findById(id);
        if (details.isPresent()) {
            return new ResponseEntity<>(details.get(), HttpStatus.OK);
        } else {
            response.setMessages(RECORD_NOT_FOUND);
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }
    }

    /**
     * Get identification details by user ID
     * Returns a list of identification detail DTOs with flat document IDs
     *
     * @param userId the ID of the user
     * @return ResponseEntity with list of UserIdentificationDetailsResponseResource
     */
    @GetMapping(value = "/get-by-user/{userId}")
    public ResponseEntity<Object> getByUserId(@PathVariable Long userId) {
        log.info("Received get identification details request for user ID: {}", userId);
        List<UserIdentificationDetailsResponseResource> details = userIdentificationDetailsService.getResponseByUserId(userId);
        if (details != null && !details.isEmpty()) {
            return new ResponseEntity<>(details, HttpStatus.OK);
        } else {
            SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
            response.setMessages(RECORD_NOT_FOUND);
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping(value = "/get-by-user/{userId}/status/{status}")
    public ResponseEntity<Object> getByUserIdAndStatus(@PathVariable Long userId, @PathVariable String status) {
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        List<UserIdentificationDetails> details = userIdentificationDetailsService.findByUserIdAndStatus(userId, status);
        if (details != null && !details.isEmpty()) {
            return new ResponseEntity<>(details, HttpStatus.OK);
        } else {
            response.setMessages(RECORD_NOT_FOUND);
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }
    }
}
