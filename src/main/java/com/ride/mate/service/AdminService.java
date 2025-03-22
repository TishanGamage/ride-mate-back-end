package com.ride.mate.service;

import com.ride.mate.resources.*;
import com.ride.mate.resources.DriverVehicleDetailsResponse;
import java.util.List;

/**
 * Admin Service
 * Service interface for all admin portal operations including dashboard,
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
public interface AdminService {

    // Authentication
    LoginResponse adminLogin(LoginRequest request);

    // Admin Setup (promote existing user to ADMIN)
    LoginResponse setupAdmin(LoginRequest request);

    // Dashboard
    AdminDashboardStatsResponse getDashboardStats();

    // User Management
    List<AdminUserResponse> getAllUsers(String role);

    AdminUserDetailResponse getUserDetail(Long userId);

    AdminUserResponse updateUserStatus(Long userId, AdminUserStatusRequest request);

    void deleteUser(Long userId, Long adminUserId);

    // Driver Management
    List<DriverProfileResponse> getPendingDrivers();

    DriverProfileResponse approveDriver(Long driverProfileId, AdminDriverApprovalRequest request);

    List<DriverVehicleDetailsResponse> getPendingVehicles();

    DriverVehicleDetailsResponse approveVehicle(Long vehicleId, AdminVehicleApprovalRequest request);

    // Ride Management
    List<AdminRideResponse> getAllRides(String status);

    AdminRideResponse getRideDetail(Long rideId);

    // Reports
    List<UserReportResponse> getAllReports(String status);

    UserReportResponse updateReportStatus(Long reportId, AdminReportStatusRequest request);

    // Feedback
    List<UserFeedbackResponse> getAllFeedback();

    // Payments
    List<AdminPaymentResponse> getAllPayments(String status);

    // Withdrawals
    List<AdminWithdrawalResponse> getAllWithdrawals(String status);

    AdminWithdrawalResponse updateWithdrawalStatus(Long withdrawalId, AdminWithdrawalStatusRequest request);
}
