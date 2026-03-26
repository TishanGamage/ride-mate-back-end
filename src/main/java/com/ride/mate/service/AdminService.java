package com.ride.mate.service;

import com.ride.mate.resources.*;

import java.util.List;

public interface AdminService {

    LoginResponse adminLogin(LoginRequest request);

    List<AdminUserResponse> getAllUsers(String role);

    AdminUserResponse updateUserStatus(Long userId, AdminUserStatusRequest request);

    List<DriverProfileResponse> getPendingDrivers();

    DriverProfileResponse approveDriver(Long driverProfileId, AdminDriverApprovalRequest request);

    List<UserReportResponse> getAllReports(String status);

    UserReportResponse updateReportStatus(Long reportId, AdminReportStatusRequest request);

    List<UserFeedbackResponse> getAllFeedback();
}
