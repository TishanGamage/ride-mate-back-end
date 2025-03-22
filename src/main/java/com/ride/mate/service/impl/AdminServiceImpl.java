package com.ride.mate.service.impl;

import com.ride.mate.core.LoginAuthentication;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.*;
import com.ride.mate.enums.*;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.*;
import com.ride.mate.resources.*;
import com.ride.mate.domain.DriverVehicleDetails;
import com.ride.mate.service.AdminService;
import com.ride.mate.service.DriverProfileService;
import com.ride.mate.util.DateUtil;
import com.ride.mate.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin Service Implementation
 * Implements all admin portal operations including dashboard statistics,
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
@Service
@Transactional
public class AdminServiceImpl extends MessagePropertyBase implements AdminService {

    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final UserReportRepository userReportRepository;
    private final UserFeedbackRepository userFeedbackRepository;
    private final RideDetailRepository rideDetailRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final WithdrawalRequestRepository withdrawalRequestRepository;
    private final DriverProfileService driverProfileService;
    private final DriverVehicleDetailsRepository driverVehicleDetailsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final Environment environment;

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    public AdminServiceImpl(UserRepository userRepository,
                            DriverProfileRepository driverProfileRepository,
                            UserReportRepository userReportRepository,
                            UserFeedbackRepository userFeedbackRepository,
                            RideDetailRepository rideDetailRepository,
                            PaymentTransactionRepository paymentTransactionRepository,
                            WithdrawalRequestRepository withdrawalRequestRepository,
                            DriverProfileService driverProfileService,
                            DriverVehicleDetailsRepository driverVehicleDetailsRepository,
                            PasswordEncoder passwordEncoder,
                            JwtUtil jwtUtil,
                            Environment environment) {
        this.userRepository = userRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.userReportRepository = userReportRepository;
        this.userFeedbackRepository = userFeedbackRepository;
        this.rideDetailRepository = rideDetailRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.withdrawalRequestRepository = withdrawalRequestRepository;
        this.driverProfileService = driverProfileService;
        this.driverVehicleDetailsRepository = driverVehicleDetailsRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.environment = environment;
    }

    // ======================== Authentication ========================

    @Override
    public LoginResponse adminLogin(LoginRequest request) {
        log.info("Processing admin login for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ValidateRecordException(environment.getProperty(LOGIN_USER_NOT_FOUND), "errorMessage"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ValidateRecordException(environment.getProperty(LOGIN_INVALID_CREDENTIALS), "errorMessage");
        }

        if (!UserRole.ADMIN.equals(user.getUserRole())) {
            log.warn("Admin login failed: User is not an admin - {}", request.getEmail());
            throw new ValidateRecordException(environment.getProperty(ADMIN_ACCESS_DENIED), "errorMessage");
        }

        if (UserStatus.INACTIVE.equals(user.getStatus())) {
            throw new ValidateRecordException(environment.getProperty(LOGIN_ACCOUNT_SUSPENDED), "errorMessage");
        }

        user.setLastLoginDate(DateUtil.getDate());
        user.setModifiedDate(DateUtil.getDate());
        user.setModifiedUser(LoginAuthentication.getUserName());
        userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getUserRole().name(), user.getFirstName());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        log.info("Admin logged in successfully: {}", user.getEmail());
        return LoginResponse.builder()
                .message(environment.getProperty(LOGIN_SUCCESS))
                .success(true)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000)
                .userId(user.getId())
                .userName(user.getFirstName())
                .email(user.getEmail())
                .role(user.getUserRole().name())
                .emailVerified(user.getEmailVerified().toString())
                .build();
    }

    @Override
    public LoginResponse setupAdmin(LoginRequest request) {
        log.info("Processing admin setup for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ValidateRecordException(environment.getProperty(LOGIN_USER_NOT_FOUND), "errorMessage"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ValidateRecordException(environment.getProperty(LOGIN_INVALID_CREDENTIALS), "errorMessage");
        }

        // Promote user to ADMIN role
        user.setUserRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        user.setLastLoginDate(DateUtil.getDate());
        user.setModifiedDate(DateUtil.getDate());
        user.setModifiedUser(SYSTEM);
        userRepository.save(user);

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getUserRole().name(), user.getFirstName());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        log.info("Admin setup completed successfully for: {}", user.getEmail());
        return LoginResponse.builder()
                .message(environment.getProperty(ADMIN_SETUP_SUCCESS))
                .success(true)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000)
                .userId(user.getId())
                .userName(user.getFirstName())
                .email(user.getEmail())
                .role(user.getUserRole().name())
                .emailVerified(user.getEmailVerified().toString())
                .build();
    }

    // ======================== Dashboard ========================

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardStatsResponse getDashboardStats() {
        log.info("Loading admin dashboard statistics");

        return AdminDashboardStatsResponse.builder()
                // User stats
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.countByStatus(UserStatus.ACTIVE))
                .inactiveUsers(userRepository.countByStatus(UserStatus.INACTIVE))
                .pendingUsers(userRepository.countByStatus(UserStatus.PENDING))
                .totalPassengers(userRepository.countByUserRole(UserRole.PASSENGER))
                .totalDrivers(userRepository.countByUserRole(UserRole.DRIVER))
                .totalAdmins(userRepository.countByUserRole(UserRole.ADMIN))
                // Driver stats
                .pendingDriverApprovals(driverProfileRepository.countByAccountStatus(DriverStatus.PENDING))
                .approvedDrivers(driverProfileRepository.countByAccountStatus(DriverStatus.APPROVED))
                .rejectedDrivers(driverProfileRepository.countByAccountStatus(DriverStatus.REJECTED))
                // Ride stats
                .totalRides(rideDetailRepository.count())
                .activeRides(rideDetailRepository.countByStatus(RideStatus.ACTIVE))
                .completedRides(rideDetailRepository.countByStatus(RideStatus.COMPLETED))
                .cancelledRides(rideDetailRepository.countByStatus(RideStatus.CANCELLED))
                // Report & feedback stats
                .totalReports(userReportRepository.count())
                .pendingReports(userReportRepository.countByStatus("PENDING"))
                .inReviewReports(userReportRepository.countByStatus("IN_REVIEW"))
                .resolvedReports(userReportRepository.countByStatus("RESOLVED"))
                .totalFeedback(userFeedbackRepository.count())
                // Financial stats
                .totalPayments(paymentTransactionRepository.count())
                .pendingWithdrawals(withdrawalRequestRepository.countByStatus(WithdrawalStatus.PENDING))
                .approvedWithdrawals(withdrawalRequestRepository.countByStatus(WithdrawalStatus.APPROVED))
                .rejectedWithdrawals(withdrawalRequestRepository.countByStatus(WithdrawalStatus.REJECTED))
                .build();
    }

    // ======================== User Management ========================

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers(String role) {
        log.info("Fetching all users, role filter: {}", role);
        List<User> users;
        if (role != null && !role.isBlank()) {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            users = userRepository.findByUserRoleOrderByCreatedDateDesc(userRole);
        } else {
            users = userRepository.findAllByOrderByCreatedDateDesc();
        }
        return users.stream().map(this::mapToAdminUserResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserDetailResponse getUserDetail(Long userId) {
        log.info("Fetching user detail for ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message"));

        AdminUserDetailResponse.AdminUserDetailResponseBuilder builder = AdminUserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .userRole(user.getUserRole() != null ? user.getUserRole().name() : null)
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .emailVerified(user.getEmailVerified() != null ? user.getEmailVerified().name() : null)
                .createdDate(user.getCreatedDate() != null ? user.getCreatedDate().toString() : null)
                .lastLoginDate(user.getLastLoginDate() != null ? user.getLastLoginDate().toString() : null)
                .modifiedDate(user.getModifiedDate() != null ? user.getModifiedDate().toString() : null)
                .totalReports(userReportRepository.countByUserId(userId))
                .totalFeedback(userFeedbackRepository.countByUserId(userId))
                .totalPayments(paymentTransactionRepository.countByUserId(userId));

        // Add driver-specific details if user is a driver
        if (UserRole.DRIVER.equals(user.getUserRole())) {
            driverProfileRepository.findByUserId(userId).ifPresent(dp -> {
                builder.driverProfileId(dp.getId())
                        .driverLicenseNumber(dp.getDriverLicenseNumber())
                        .driverLicenseExpiry(dp.getDriverLicenseExpiry() != null ? dp.getDriverLicenseExpiry().toString() : null)
                        .driverLicenseVerified(dp.getDriverLicenseVerified() != null ? dp.getDriverLicenseVerified().name() : null)
                        .accountStatus(dp.getAccountStatus() != null ? dp.getAccountStatus().name() : null)
                        .ratingAsDriver(dp.getRatingAsDriver() != null ? dp.getRatingAsDriver().toPlainString() : null)
                        .totalRidesAsDriver(dp.getTotalRidesAsDriver())
                        .totalEarnings(dp.getTotalEarnings() != null ? dp.getTotalEarnings().toPlainString() : null)
                        .driverProfileCompleted(dp.getDriverProfileCompleted());

                // Fetch recent rides for driver
                List<RideDetail> driverRides = rideDetailRepository.findByDriverProfileId(dp.getId());
                if (driverRides != null && !driverRides.isEmpty()) {
                    List<AdminRideResponse> recentRides = driverRides.stream()
                            .limit(5)
                            .map(this::mapToAdminRideResponse)
                            .collect(Collectors.toList());
                    builder.recentRides(recentRides);
                }
            });
        }

        log.info("User detail loaded for ID: {}", userId);
        return builder.build();
    }

    @Override
    public AdminUserResponse updateUserStatus(Long userId, AdminUserStatusRequest request) {
        log.info("Updating status for user ID: {} to {}", userId, request.getStatus());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message"));

        user.setStatus(UserStatus.valueOf(request.getStatus().toUpperCase()));
        user.setModifiedDate(DateUtil.getDate());
        user.setModifiedUser(LoginAuthentication.getUserName());
        User updated = userRepository.save(user);

        log.info("User status updated for ID: {}", userId);
        return mapToAdminUserResponse(updated);
    }

    @Override
    public void deleteUser(Long userId, Long adminUserId) {
        log.info("Deleting user ID: {} by admin ID: {}", userId, adminUserId);

        if (userId.equals(adminUserId)) {
            log.warn("Admin attempted to delete own account: {}", adminUserId);
            throw new ValidateRecordException(environment.getProperty(ADMIN_CANNOT_DELETE_SELF), "message");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message"));

        userRepository.delete(user);
        log.info("User deleted successfully with ID: {}", userId);
    }

    // ======================== Driver Management ========================

    @Override
    @Transactional(readOnly = true)
    public List<DriverProfileResponse> getPendingDrivers() {
        log.info("Fetching pending driver profiles");
        return driverProfileRepository.findByAccountStatus(DriverStatus.PENDING)
                .stream()
                .map(dp -> driverProfileService.getDriverProfileByUserId(dp.getUser().getId()))
                .collect(Collectors.toList());
    }

    @Override
    public DriverProfileResponse approveDriver(Long driverProfileId, AdminDriverApprovalRequest request) {
        log.info("Processing driver approval for profile ID: {}, status: {}", driverProfileId, request.getAccountStatus());

        DriverProfile driverProfile = driverProfileRepository.findById(driverProfileId)
                .orElseThrow(() -> new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message"));

        driverProfile.setAccountStatus(DriverStatus.valueOf(request.getAccountStatus().toUpperCase()));
        driverProfile.setApprovedBy(LoginAuthentication.getUserName());
        driverProfile.setApprovedDate(DateUtil.getDate());
        driverProfile.setModifiedDate(DateUtil.getDate());
        driverProfile.setModifiedUser(LoginAuthentication.getUserName());
        driverProfile.setSyncTs(DateUtil.getDate());
        driverProfileRepository.save(driverProfile);

        // If approved, activate the user account
        if (DriverStatus.APPROVED.name().equalsIgnoreCase(request.getAccountStatus())) {
            User user = driverProfile.getUser();
            user.setStatus(UserStatus.ACTIVE);
            user.setModifiedDate(DateUtil.getDate());
            user.setModifiedUser(LoginAuthentication.getUserName());
            userRepository.save(user);
        }

        log.info("Driver profile {} updated to status: {}", driverProfileId, request.getAccountStatus());
        return driverProfileService.getDriverProfileByUserId(driverProfile.getUser().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DriverVehicleDetailsResponse> getPendingVehicles() {
        log.info("Fetching pending vehicles for admin review");
        return driverVehicleDetailsRepository.findByStatusWithDocuments("PENDING")
                .stream()
                .map(this::mapToVehicleResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DriverVehicleDetailsResponse approveVehicle(Long vehicleId, AdminVehicleApprovalRequest request) {
        log.info("Processing vehicle approval for vehicle ID: {}, status: {}", vehicleId, request.getStatus());

        DriverVehicleDetails vehicle = driverVehicleDetailsRepository.findByIdWithDocuments(vehicleId)
                .orElseThrow(() -> new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message"));

        String status = request.getStatus().toUpperCase();
        vehicle.setStatus(status);

        if ("APPROVED".equals(status)) {
            vehicle.setIsVerified(YesNo.YES);
            vehicle.setVerifiedBy(LoginAuthentication.getUserName());
            vehicle.setVerifiedDate(DateUtil.getDate());
            vehicle.setRejectionReason(null);
        } else if ("REJECTED".equals(status)) {
            vehicle.setIsVerified(YesNo.NO);
            vehicle.setRejectionReason(request.getRejectionReason());
        }

        vehicle.setModifiedDate(DateUtil.getDate());
        vehicle.setModifiedUser(LoginAuthentication.getUserName());
        vehicle.setSyncTs(DateUtil.getDate());

        DriverVehicleDetails updated = driverVehicleDetailsRepository.save(vehicle);
        log.info("Vehicle ID: {} updated to status: {}", vehicleId, status);
        return mapToVehicleResponse(updated);
    }

    // ======================== Ride Management ========================

    @Override
    @Transactional(readOnly = true)
    public List<AdminRideResponse> getAllRides(String status) {
        log.info("Fetching all rides, status filter: {}", status);
        List<RideDetail> rides;
        if (status != null && !status.isBlank()) {
            RideStatus rideStatus = RideStatus.valueOf(status.toUpperCase());
            rides = rideDetailRepository.findByStatusOrderByCreatedDateDesc(rideStatus);
        } else {
            rides = rideDetailRepository.findAllByOrderByCreatedDateDesc();
        }
        return rides.stream().map(this::mapToAdminRideResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AdminRideResponse getRideDetail(Long rideId) {
        log.info("Fetching ride detail for ID: {}", rideId);
        RideDetail ride = rideDetailRepository.findById(rideId)
                .orElseThrow(() -> new ValidateRecordException(environment.getProperty(RIDE_NOT_FOUND), "message"));
        return mapToAdminRideResponse(ride);
    }

    // ======================== Reports ========================

    @Override
    @Transactional(readOnly = true)
    public List<UserReportResponse> getAllReports(String status) {
        log.info("Fetching all reports, status filter: {}", status);
        List<UserReport> reports;
        if (status != null && !status.isBlank()) {
            reports = userReportRepository.findByStatusOrderByCreatedDateDesc(status.toUpperCase());
        } else {
            reports = userReportRepository.findAllByOrderByCreatedDateDesc();
        }
        return reports.stream().map(this::mapToReportResponse).collect(Collectors.toList());
    }

    @Override
    public UserReportResponse updateReportStatus(Long reportId, AdminReportStatusRequest request) {
        log.info("Updating report ID: {} to status: {}", reportId, request.getStatus());
        UserReport report = userReportRepository.findById(reportId)
                .orElseThrow(() -> new ValidateRecordException(environment.getProperty(RECORD_NOT_FOUND), "message"));

        if ("CLOSED".equalsIgnoreCase(report.getStatus())) {
            log.warn("Attempted to update closed report ID: {}", reportId);
            throw new ValidateRecordException(environment.getProperty(USER_REPORT_ALREADY_CLOSED), "message");
        }

        report.setStatus(request.getStatus().toUpperCase());
        report.setModifiedDate(DateUtil.getDate());
        report.setModifiedUser(LoginAuthentication.getUserName());
        UserReport updated = userReportRepository.save(report);

        log.info("Report status updated for ID: {}", reportId);
        return mapToReportResponse(updated);
    }

    // ======================== Feedback ========================

    @Override
    @Transactional(readOnly = true)
    public List<UserFeedbackResponse> getAllFeedback() {
        log.info("Fetching all user feedback");
        return userFeedbackRepository.findAllByOrderByCreatedDateDesc()
                .stream().map(this::mapToFeedbackResponse).collect(Collectors.toList());
    }

    // ======================== Payments ========================

    @Override
    @Transactional(readOnly = true)
    public List<AdminPaymentResponse> getAllPayments(String status) {
        log.info("Fetching all payments, status filter: {}", status);
        List<PaymentTransaction> payments;
        if (status != null && !status.isBlank()) {
            PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            payments = paymentTransactionRepository.findByStatusOrderByCreatedDateDesc(paymentStatus);
        } else {
            payments = paymentTransactionRepository.findAllByOrderByCreatedDateDesc();
        }
        return payments.stream().map(this::mapToAdminPaymentResponse).collect(Collectors.toList());
    }

    // ======================== Withdrawals ========================

    @Override
    @Transactional(readOnly = true)
    public List<AdminWithdrawalResponse> getAllWithdrawals(String status) {
        log.info("Fetching all withdrawals, status filter: {}", status);
        List<WithdrawalRequest> withdrawals;
        if (status != null && !status.isBlank()) {
            WithdrawalStatus withdrawalStatus = WithdrawalStatus.valueOf(status.toUpperCase());
            withdrawals = withdrawalRequestRepository.findByStatus(withdrawalStatus);
        } else {
            withdrawals = withdrawalRequestRepository.findAllByOrderByCreatedDateDesc();
        }
        return withdrawals.stream().map(this::mapToAdminWithdrawalResponse).collect(Collectors.toList());
    }

    @Override
    public AdminWithdrawalResponse updateWithdrawalStatus(Long withdrawalId, AdminWithdrawalStatusRequest request) {
        log.info("Updating withdrawal ID: {} to status: {}", withdrawalId, request.getStatus());

        WithdrawalRequest withdrawal = withdrawalRequestRepository.findById(withdrawalId)
                .orElseThrow(() -> new ValidateRecordException(environment.getProperty(WITHDRAWAL_REQUEST_NOT_FOUND), "message"));

        if (!WithdrawalStatus.PENDING.equals(withdrawal.getStatus())) {
            log.warn("Attempted to update already processed withdrawal ID: {}", withdrawalId);
            throw new ValidateRecordException(environment.getProperty(ADMIN_WITHDRAWAL_ALREADY_PROCESSED), "message");
        }

        withdrawal.setStatus(WithdrawalStatus.valueOf(request.getStatus().toUpperCase()));
        if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
            withdrawal.setRemarks(request.getRemarks());
        }
        withdrawal.setModifiedDate(DateUtil.getDate());
        withdrawal.setModifiedUser(LoginAuthentication.getUserName());
        WithdrawalRequest updated = withdrawalRequestRepository.save(withdrawal);

        log.info("Withdrawal status updated for ID: {}", withdrawalId);
        return mapToAdminWithdrawalResponse(updated);
    }

    // ======================== Mappers ========================

    private AdminUserResponse mapToAdminUserResponse(User u) {
        return AdminUserResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .firstName(u.getFirstName())
                .lastName(u.getLastName())
                .phoneNumber(u.getPhoneNumber())
                .userRole(u.getUserRole() != null ? u.getUserRole().name() : null)
                .status(u.getStatus() != null ? u.getStatus().name() : null)
                .emailVerified(u.getEmailVerified() != null ? u.getEmailVerified().name() : null)
                .createdDate(u.getCreatedDate() != null ? u.getCreatedDate().toString() : null)
                .lastLoginDate(u.getLastLoginDate() != null ? u.getLastLoginDate().toString() : null)
                .build();
    }

    private AdminRideResponse mapToAdminRideResponse(RideDetail r) {
        String driverName = null;
        String driverEmail = null;
        if (r.getDriverProfile() != null && r.getDriverProfile().getUser() != null) {
            User driverUser = r.getDriverProfile().getUser();
            driverName = driverUser.getFirstName() + " " + driverUser.getLastName();
            driverEmail = driverUser.getEmail();
        }

        return AdminRideResponse.builder()
                .id(r.getId())
                .driverProfileId(r.getDriverProfile() != null ? r.getDriverProfile().getId() : null)
                .driverName(driverName)
                .driverEmail(driverEmail)
                .startCity(r.getStartCity())
                .endCity(r.getEndCity())
                .startLocationLatitude(r.getStartLocationLatitude() != null ? r.getStartLocationLatitude().toPlainString() : null)
                .startLocationLongitude(r.getStartLocationLongitude() != null ? r.getStartLocationLongitude().toPlainString() : null)
                .endLocationLatitude(r.getEndLocationLatitude() != null ? r.getEndLocationLatitude().toPlainString() : null)
                .endLocationLongitude(r.getEndLocationLongitude() != null ? r.getEndLocationLongitude().toPlainString() : null)
                .availableSeats(r.getAvailableSeats())
                .totalRideDistance(r.getTotalRideDistance() != null ? r.getTotalRideDistance().toPlainString() : null)
                .totalRideCost(r.getTotalRideCost() != null ? r.getTotalRideCost().toPlainString() : null)
                .perKmRate(r.getPerKmRate() != null ? r.getPerKmRate().toPlainString() : null)
                .status(r.getStatus() != null ? r.getStatus().name() : null)
                .startTime(r.getStartTime() != null ? r.getStartTime().toString() : null)
                .createdDate(r.getCreatedDate() != null ? r.getCreatedDate().toString() : null)
                .build();
    }

    private UserReportResponse mapToReportResponse(UserReport r) {
        return UserReportResponse.builder()
                .id(r.getId())
                .userId(r.getUser().getId())
                .userFullName(r.getUser().getFirstName() + " " + r.getUser().getLastName())
                .category(r.getCategory())
                .subject(r.getSubject())
                .description(r.getDescription())
                .status(r.getStatus())
                .createdDate(r.getCreatedDate() != null ? r.getCreatedDate().toString() : null)
                .build();
    }

    private UserFeedbackResponse mapToFeedbackResponse(UserFeedback f) {
        return UserFeedbackResponse.builder()
                .id(f.getId())
                .userId(f.getUser().getId())
                .userFullName(f.getUser().getFirstName() + " " + f.getUser().getLastName())
                .rating(f.getRating())
                .category(f.getCategory())
                .feedbackText(f.getFeedbackText())
                .createdDate(f.getCreatedDate() != null ? f.getCreatedDate().toString() : null)
                .build();
    }

    private AdminPaymentResponse mapToAdminPaymentResponse(PaymentTransaction p) {
        String userFullName = null;
        String userEmail = null;
        if (p.getUser() != null) {
            userFullName = p.getUser().getFirstName() + " " + p.getUser().getLastName();
            userEmail = p.getUser().getEmail();
        }

        return AdminPaymentResponse.builder()
                .id(p.getId())
                .userId(p.getUser() != null ? p.getUser().getId() : null)
                .userFullName(userFullName)
                .userEmail(userEmail)
                .rideDetailId(p.getRideDetail() != null ? p.getRideDetail().getId() : null)
                .orderId(p.getOrderId())
                .paymentId(p.getPaymentId())
                .amount(p.getPayhereAmount() != null ? p.getPayhereAmount().toPlainString() : null)
                .currency(p.getCurrency())
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .method(p.getMethod())
                .createdDate(p.getCreatedDate() != null ? p.getCreatedDate().toString() : null)
                .build();
    }

    private AdminWithdrawalResponse mapToAdminWithdrawalResponse(WithdrawalRequest w) {
        String driverName = null;
        String driverEmail = null;
        if (w.getDriverProfile() != null && w.getDriverProfile().getUser() != null) {
            User driverUser = w.getDriverProfile().getUser();
            driverName = driverUser.getFirstName() + " " + driverUser.getLastName();
            driverEmail = driverUser.getEmail();
        }

        return AdminWithdrawalResponse.builder()
                .id(w.getId())
                .driverProfileId(w.getDriverProfile() != null ? w.getDriverProfile().getId() : null)
                .driverName(driverName)
                .driverEmail(driverEmail)
                .amount(w.getAmount() != null ? w.getAmount().toPlainString() : null)
                .currency(w.getCurrency())
                .bankName(w.getBankName())
                .accountNumber(w.getAccountNumber())
                .accountHolderName(w.getAccountHolderName())
                .status(w.getStatus() != null ? w.getStatus().name() : null)
                .remarks(w.getRemarks())
                .createdDate(w.getCreatedDate() != null ? w.getCreatedDate().toString() : null)
                .build();
    }

    private DriverVehicleDetailsResponse mapToVehicleResponse(DriverVehicleDetails v) {
        return DriverVehicleDetailsResponse.builder()
                .id(v.getId())
                .vehicleTypeId(v.getVehicleType() != null ? v.getVehicleType().getId() : null)
                .vehicleTypeName(v.getVehicleType() != null ? v.getVehicleType().getName() : null)
                .vehicleMakeId(v.getVehicleMake() != null ? v.getVehicleMake().getId() : null)
                .vehicleMakeName(v.getVehicleMake() != null ? v.getVehicleMake().getName() : null)
                .vehicleModelId(v.getVehicleModel() != null ? v.getVehicleModel().getId() : null)
                .vehicleModelName(v.getVehicleModel() != null ? v.getVehicleModel().getName() : null)
                .registrationNumber(v.getRegistrationNumber())
                .model(v.getModel())
                .year(v.getYear())
                .color(v.getColor())
                .seats(v.getSeats())
                .vehicleImageDocumentId1(v.getVehicleImageDocument1() != null ? v.getVehicleImageDocument1().getId() : null)
                .vehicleImageUrl1(v.getVehicleImageDocument1() != null ? v.getVehicleImageDocument1().getDocumentUrl() : null)
                .vehicleImageDocumentId2(v.getVehicleImageDocument2() != null ? v.getVehicleImageDocument2().getId() : null)
                .vehicleImageUrl2(v.getVehicleImageDocument2() != null ? v.getVehicleImageDocument2().getDocumentUrl() : null)
                .vehicleImageDocumentId3(v.getVehicleImageDocument3() != null ? v.getVehicleImageDocument3().getId() : null)
                .vehicleImageUrl3(v.getVehicleImageDocument3() != null ? v.getVehicleImageDocument3().getDocumentUrl() : null)
                .vehicleImageDocumentId4(v.getVehicleImageDocument4() != null ? v.getVehicleImageDocument4().getId() : null)
                .vehicleImageUrl4(v.getVehicleImageDocument4() != null ? v.getVehicleImageDocument4().getDocumentUrl() : null)
                .registrationCertificateDocumentId(v.getRegistrationCertificateDocument() != null ? v.getRegistrationCertificateDocument().getId() : null)
                .registrationCertificateUrl(v.getRegistrationCertificateDocument() != null ? v.getRegistrationCertificateDocument().getDocumentUrl() : null)
                .insuranceNumber(v.getInsuranceNumber())
                .insuranceProvider(v.getInsuranceProvider())
                .insuranceExpiry(v.getInsuranceExpiry() != null ? v.getInsuranceExpiry().toString() : null)
                .insuranceDocumentId1(v.getInsuranceDocument1() != null ? v.getInsuranceDocument1().getId() : null)
                .insuranceDocumentUrl1(v.getInsuranceDocument1() != null ? v.getInsuranceDocument1().getDocumentUrl() : null)
                .insuranceDocumentId2(v.getInsuranceDocument2() != null ? v.getInsuranceDocument2().getId() : null)
                .insuranceDocumentUrl2(v.getInsuranceDocument2() != null ? v.getInsuranceDocument2().getDocumentUrl() : null)
                .revenueLicenseDocumentId1(v.getRevenueLicenseDocument1() != null ? v.getRevenueLicenseDocument1().getId() : null)
                .revenueLicenseDocumentUrl1(v.getRevenueLicenseDocument1() != null ? v.getRevenueLicenseDocument1().getDocumentUrl() : null)
                .revenueLicenseDocumentId2(v.getRevenueLicenseDocument2() != null ? v.getRevenueLicenseDocument2().getId() : null)
                .revenueLicenseDocumentUrl2(v.getRevenueLicenseDocument2() != null ? v.getRevenueLicenseDocument2().getDocumentUrl() : null)
                .isVerified(v.getIsVerified() != null ? v.getIsVerified().name() : null)
                .isPrimary(v.getIsPrimary() != null ? v.getIsPrimary().name() : null)
                .status(v.getStatus())
                .createdDate(v.getCreatedDate() != null ? v.getCreatedDate().toString() : null)
                .modifiedDate(v.getModifiedDate() != null ? v.getModifiedDate().toString() : null)
                .build();
    }
}

