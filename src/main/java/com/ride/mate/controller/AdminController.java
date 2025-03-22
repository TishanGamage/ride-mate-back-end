package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.resources.*;
import com.ride.mate.resources.DriverVehicleDetailsResponse;
import com.ride.mate.service.AdminService;
import com.ride.mate.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin Controller
 * REST API endpoints for admin web portal operations: login, dashboard,
 * user management, driver approval, ride oversight, reports, feedback,
 * payments, and withdrawal management.
 *
 * @author RideMate
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 02-04-2026    N/A          N/A          RideMate         Initial Development
 * 2 02-04-2026    N/A          N/A          RideMate         Added dashboard, rides, payments, withdrawals
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController extends MessagePropertyBase {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;
    private final Environment environment;

    public AdminController(AdminService adminService, JwtUtil jwtUtil, Environment environment) {
        this.adminService = adminService;
        this.jwtUtil = jwtUtil;
        this.environment = environment;
    }

    // ======================== Authentication ========================

    /**
     * POST /admin/login
     * Admin-specific login — validates ADMIN role before issuing tokens.
     *
     * @param request login credentials
     * @return ResponseEntity with JWT tokens and user info
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> adminLogin(@Valid @RequestBody LoginRequest request) {
        log.info("Admin login request for email: {}", request.getEmail());
        return new ResponseEntity<>(adminService.adminLogin(request), HttpStatus.OK);
    }

    /**
     * POST /admin/setup
     * Promote an existing user to ADMIN role. Requires valid email and password.
     * This endpoint is public and should be removed or secured after initial setup.
     *
     * @param request login credentials of the user to promote
     * @return ResponseEntity with JWT tokens and user info
     */
    @PostMapping("/setup")
    public ResponseEntity<LoginResponse> setupAdmin(@Valid @RequestBody LoginRequest request) {
        log.info("Admin setup request for email: {}", request.getEmail());
        return new ResponseEntity<>(adminService.setupAdmin(request), HttpStatus.CREATED);
    }

    // ======================== Dashboard ========================

    /**
     * GET /admin/dashboard
     * Retrieve aggregated statistics for the admin dashboard.
     *
     * @return ResponseEntity with dashboard statistics
     */
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardStatsResponse> getDashboardStats() {
        log.info("GET /admin/dashboard");
        return new ResponseEntity<>(adminService.getDashboardStats(), HttpStatus.OK);
    }

    // ======================== User Management ========================

    /**
     * GET /admin/users?role=PASSENGER|DRIVER|ADMIN
     * List all registered users, optionally filtered by role.
     *
     * @param role optional role filter
     * @return ResponseEntity with list of users
     */
    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers(
            @RequestParam(required = false) String role) {
        log.info("GET /admin/users, role={}", role);
        return new ResponseEntity<>(adminService.getAllUsers(role), HttpStatus.OK);
    }

    /**
     * GET /admin/users/{id}
     * Retrieve detailed information for a specific user.
     *
     * @param id user ID
     * @return ResponseEntity with user detail
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserDetailResponse> getUserDetail(@PathVariable Long id) {
        log.info("GET /admin/users/{}", id);
        return new ResponseEntity<>(adminService.getUserDetail(id), HttpStatus.OK);
    }

    /**
     * PUT /admin/users/{id}/status
     * Activate, suspend, or deactivate a user account.
     *
     * @param id user ID
     * @param request status update request
     * @return ResponseEntity with updated user
     */
    @PutMapping("/users/{id}/status")
    public ResponseEntity<AdminUserResponse> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserStatusRequest request) {
        log.info("PUT /admin/users/{}/status -> {}", id, request.getStatus());
        return new ResponseEntity<>(adminService.updateUserStatus(id, request), HttpStatus.OK);
    }

    /**
     * DELETE /admin/users/{id}
     * Delete a user account (admin cannot delete themselves).
     *
     * @param id user ID to delete
     * @param httpRequest HTTP request to extract admin user ID from JWT
     * @return ResponseEntity with success message
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<SuccessAndErrorDetailsResource> deleteUser(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        log.info("DELETE /admin/users/{}", id);

        Long adminUserId = extractUserIdFromRequest(httpRequest);
        adminService.deleteUser(id, adminUserId);

        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setMessages(environment.getProperty(ADMIN_USER_DELETED));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ======================== Driver Management ========================

    /**
     * GET /admin/drivers/pending
     * List all driver profiles with PENDING account status.
     *
     * @return ResponseEntity with pending driver profiles
     */
    @GetMapping("/drivers/pending")
    public ResponseEntity<List<DriverProfileResponse>> getPendingDrivers() {
        log.info("GET /admin/drivers/pending");
        return new ResponseEntity<>(adminService.getPendingDrivers(), HttpStatus.OK);
    }

    /**
     * PUT /admin/drivers/{driverProfileId}/approve
     * Approve or reject a driver profile (APPROVED | REJECTED).
     *
     * @param driverProfileId driver profile ID
     * @param request approval request
     * @return ResponseEntity with updated driver profile
     */
    @PutMapping("/drivers/{driverProfileId}/approve")
    public ResponseEntity<DriverProfileResponse> approveDriver(
            @PathVariable Long driverProfileId,
            @Valid @RequestBody AdminDriverApprovalRequest request) {
        log.info("PUT /admin/drivers/{}/approve -> {}", driverProfileId, request.getAccountStatus());
        return new ResponseEntity<>(adminService.approveDriver(driverProfileId, request), HttpStatus.OK);
    }

    /**
     * GET /admin/drivers/vehicles/pending
     * List all vehicle records with PENDING status awaiting admin review.
     *
     * @return ResponseEntity with list of pending vehicles
     */
    @GetMapping("/drivers/vehicles/pending")
    public ResponseEntity<List<DriverVehicleDetailsResponse>> getPendingVehicles() {
        log.info("GET /admin/drivers/vehicles/pending");
        return new ResponseEntity<>(adminService.getPendingVehicles(), HttpStatus.OK);
    }

    /**
     * PUT /admin/vehicles/{vehicleId}/approve
     * Approve or reject a driver vehicle (APPROVED | REJECTED).
     *
     * @param vehicleId vehicle ID
     * @param request approval request with status and optional rejectionReason
     * @return ResponseEntity with updated vehicle details
     */
    @PutMapping("/vehicles/{vehicleId}/approve")
    public ResponseEntity<DriverVehicleDetailsResponse> approveVehicle(
            @PathVariable Long vehicleId,
            @Valid @RequestBody AdminVehicleApprovalRequest request) {
        log.info("PUT /admin/vehicles/{}/approve -> {}", vehicleId, request.getStatus());
        return new ResponseEntity<>(adminService.approveVehicle(vehicleId, request), HttpStatus.OK);
    }

    // ======================== Ride Management ========================

    /**
     * GET /admin/rides?status=ACTIVE|COMPLETED|CANCELLED
     * List all rides, optionally filtered by status.
     *
     * @param status optional status filter
     * @return ResponseEntity with list of rides
     */
    @GetMapping("/rides")
    public ResponseEntity<List<AdminRideResponse>> getAllRides(
            @RequestParam(required = false) String status) {
        log.info("GET /admin/rides, status={}", status);
        return new ResponseEntity<>(adminService.getAllRides(status), HttpStatus.OK);
    }

    /**
     * GET /admin/rides/{id}
     * Retrieve details for a specific ride.
     *
     * @param id ride ID
     * @return ResponseEntity with ride detail
     */
    @GetMapping("/rides/{id}")
    public ResponseEntity<AdminRideResponse> getRideDetail(@PathVariable Long id) {
        log.info("GET /admin/rides/{}", id);
        return new ResponseEntity<>(adminService.getRideDetail(id), HttpStatus.OK);
    }

    // ======================== Reports ========================

    /**
     * GET /admin/reports?status=PENDING|IN_REVIEW|RESOLVED|CLOSED
     * List all user problem reports, optionally filtered by status.
     *
     * @param status optional status filter
     * @return ResponseEntity with list of reports
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
     *
     * @param id report ID
     * @param request status update request
     * @return ResponseEntity with updated report
     */
    @PutMapping("/reports/{id}/status")
    public ResponseEntity<UserReportResponse> updateReportStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminReportStatusRequest request) {
        log.info("PUT /admin/reports/{}/status -> {}", id, request.getStatus());
        return new ResponseEntity<>(adminService.updateReportStatus(id, request), HttpStatus.OK);
    }

    // ======================== Feedback ========================

    /**
     * GET /admin/feedback
     * List all user feedback submissions.
     *
     * @return ResponseEntity with list of feedback
     */
    @GetMapping("/feedback")
    public ResponseEntity<List<UserFeedbackResponse>> getAllFeedback() {
        log.info("GET /admin/feedback");
        return new ResponseEntity<>(adminService.getAllFeedback(), HttpStatus.OK);
    }

    // ======================== Payments ========================

    /**
     * GET /admin/payments?status=PENDING|SUCCESS|FAILED|REFUNDED
     * List all payment transactions, optionally filtered by status.
     *
     * @param status optional status filter
     * @return ResponseEntity with list of payments
     */
    @GetMapping("/payments")
    public ResponseEntity<List<AdminPaymentResponse>> getAllPayments(
            @RequestParam(required = false) String status) {
        log.info("GET /admin/payments, status={}", status);
        return new ResponseEntity<>(adminService.getAllPayments(status), HttpStatus.OK);
    }

    // ======================== Withdrawals ========================

    /**
     * GET /admin/withdrawals?status=PENDING|APPROVED|REJECTED
     * List all withdrawal requests, optionally filtered by status.
     *
     * @param status optional status filter
     * @return ResponseEntity with list of withdrawals
     */
    @GetMapping("/withdrawals")
    public ResponseEntity<List<AdminWithdrawalResponse>> getAllWithdrawals(
            @RequestParam(required = false) String status) {
        log.info("GET /admin/withdrawals, status={}", status);
        return new ResponseEntity<>(adminService.getAllWithdrawals(status), HttpStatus.OK);
    }

    /**
     * PUT /admin/withdrawals/{id}/status
     * Approve or reject a withdrawal request.
     *
     * @param id withdrawal request ID
     * @param request status update request
     * @return ResponseEntity with updated withdrawal
     */
    @PutMapping("/withdrawals/{id}/status")
    public ResponseEntity<AdminWithdrawalResponse> updateWithdrawalStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminWithdrawalStatusRequest request) {
        log.info("PUT /admin/withdrawals/{}/status -> {}", id, request.getStatus());
        return new ResponseEntity<>(adminService.updateWithdrawalStatus(id, request), HttpStatus.OK);
    }

    // ======================== Helper Methods ========================

    /**
     * Extract user ID from JWT token in Authorization header.
     *
     * @param request HTTP request
     * @return user ID from token
     */
    private Long extractUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.extractUserId(token);
        }
        return null;
    }
}
