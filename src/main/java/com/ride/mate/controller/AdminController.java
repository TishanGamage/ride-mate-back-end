package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.resources.*;
import com.ride.mate.service.AdminService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin Controller
 * REST API endpoints for admin operations: login, user management,
 * driver approval, reports, and feedback.
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 2026        N/A          N/A          RideMate         Initial Development
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController extends MessagePropertyBase {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * POST /admin/login
     * Admin-specific login — validates ADMIN role before issuing tokens.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> adminLogin(@Valid @RequestBody LoginRequest request) {
        log.info("Admin login request for email: {}", request.getEmail());
        return new ResponseEntity<>(adminService.adminLogin(request), HttpStatus.OK);
    }

    /**
     * GET /admin/users?role=PASSENGER|DRIVER|ADMIN
     * List all registered users, optionally filtered by role.
     */
    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers(
            @RequestParam(required = false) String role) {
        log.info("GET /admin/users, role={}", role);
        return new ResponseEntity<>(adminService.getAllUsers(role), HttpStatus.OK);
    }

    /**
     * PUT /admin/users/{id}/status
     * Activate, suspend, or deactivate a user account.
     */
    @PutMapping("/users/{id}/status")
    public ResponseEntity<AdminUserResponse> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserStatusRequest request) {
        log.info("PUT /admin/users/{}/status -> {}", id, request.getStatus());
        return new ResponseEntity<>(adminService.updateUserStatus(id, request), HttpStatus.OK);
    }

    /**
     * GET /admin/drivers/pending
     * List all driver profiles with PENDING account status.
     */
    @GetMapping("/drivers/pending")
    public ResponseEntity<List<DriverProfileResponse>> getPendingDrivers() {
        log.info("GET /admin/drivers/pending");
        return new ResponseEntity<>(adminService.getPendingDrivers(), HttpStatus.OK);
    }

    /**
     * PUT /admin/drivers/{driverProfileId}/approve
     * Approve or reject a driver profile (APPROVED | REJECTED | SUSPENDED).
     */
    @PutMapping("/drivers/{driverProfileId}/approve")
    public ResponseEntity<DriverProfileResponse> approveDriver(
            @PathVariable Long driverProfileId,
            @Valid @RequestBody AdminDriverApprovalRequest request) {
        log.info("PUT /admin/drivers/{}/approve -> {}", driverProfileId, request.getAccountStatus());
        return new ResponseEntity<>(adminService.approveDriver(driverProfileId, request), HttpStatus.OK);
    }

    /**
     * GET /admin/reports?status=PENDING|IN_REVIEW|RESOLVED|CLOSED
     * List all user problem reports, optionally filtered by status.
     */
    @GetMapping("/reports")
    public ResponseEntity<List<UserReportResponse>> getAllReports(
            @RequestParam(required = false) String status) {
        log.info("GET /admin/reports, status={}", status);
        return new ResponseEntity<>(adminService.getAllReports(status), HttpStatus.OK);
    }

    /**
     * PUT /admin/reports/{id}/status
     * Update the status of a user report (IN_REVIEW | RESOLVED | CLOSED).
     */
    @PutMapping("/reports/{id}/status")
    public ResponseEntity<UserReportResponse> updateReportStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminReportStatusRequest request) {
        log.info("PUT /admin/reports/{}/status -> {}", id, request.getStatus());
        return new ResponseEntity<>(adminService.updateReportStatus(id, request), HttpStatus.OK);
    }

    /**
     * GET /admin/feedback
     * List all user feedback submissions.
     */
    @GetMapping("/feedback")
    public ResponseEntity<List<UserFeedbackResponse>> getAllFeedback() {
        log.info("GET /admin/feedback");
        return new ResponseEntity<>(adminService.getAllFeedback(), HttpStatus.OK);
    }
}
