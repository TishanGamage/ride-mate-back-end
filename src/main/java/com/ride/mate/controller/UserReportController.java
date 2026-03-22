package com.ride.mate.controller;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.resources.UserReportRequestResource;
import com.ride.mate.resources.UserReportResponse;
import com.ride.mate.service.UserReportService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
/**
 * User Report Controller
 * REST endpoints for submitting and retrieving user problem reports.
 *
 * POST  /user-reports           - Submit a new problem report
 * GET   /user-reports/user/{userId} - Get all reports by a user
 */
@Slf4j
@RestController
@RequestMapping("/user-reports")
@CrossOrigin(origins = "*")
public class UserReportController extends MessagePropertyBase {
    private final UserReportService userReportService;
    public UserReportController(UserReportService userReportService) {
        this.userReportService = userReportService;
    }
    @PostMapping
    public ResponseEntity<UserReportResponse> submitReport(
            @Valid @RequestBody UserReportRequestResource resource) {
        log.info("POST /user-reports - userId: {}", resource.getUserId());
        UserReportResponse response = userReportService.submitReport(resource);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserReportResponse>> getReportsByUser(@PathVariable Long userId) {
        log.info("GET /user-reports/user/{}", userId);
        List<UserReportResponse> responses = userReportService.getReportsByUser(userId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
}
