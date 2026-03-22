package com.ride.mate.controller;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.resources.UserFeedbackRequestResource;
import com.ride.mate.resources.UserFeedbackResponse;
import com.ride.mate.service.UserFeedbackService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/**
 * User Feedback Controller
 * REST endpoints for submitting and retrieving user feedback.
 *
 * POST  /user-feedback           - Submit new feedback
 * GET   /user-feedback/user/{userId} - Get all feedback by a user
 */
@Slf4j
@RestController
@RequestMapping("/user-feedback")
@CrossOrigin(origins = "*")
public class UserFeedbackController extends MessagePropertyBase {
    private final UserFeedbackService userFeedbackService;
    public UserFeedbackController(UserFeedbackService userFeedbackService) {
        this.userFeedbackService = userFeedbackService;
    }
    @PostMapping("/feedback")
    public ResponseEntity<UserFeedbackResponse> submitFeedback(
            @Valid @RequestBody UserFeedbackRequestResource resource) {
        log.info("POST /user-feedback - userId: {}, rating: {}", resource.getUserId(), resource.getRating());
        UserFeedbackResponse response = userFeedbackService.submitFeedback(resource);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserFeedbackResponse>> getFeedbackByUser(@PathVariable Long userId) {
        log.info("GET /user-feedback/user/{}", userId);
        List<UserFeedbackResponse> responses = userFeedbackService.getFeedbackByUser(userId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
}
