package com.ride.mate.service.impl;

import com.ride.mate.core.LoginAuthentication;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.DriverProfile;
import com.ride.mate.domain.User;
import com.ride.mate.domain.UserReport;
import com.ride.mate.enums.DriverStatus;
import com.ride.mate.enums.UserRole;
import com.ride.mate.enums.UserStatus;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.*;
import com.ride.mate.resources.*;
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

@Slf4j
@Service
@Transactional
public class AdminServiceImpl extends MessagePropertyBase implements AdminService {

    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final UserReportRepository userReportRepository;
    private final UserFeedbackRepository userFeedbackRepository;
    private final DriverProfileService driverProfileService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final Environment environment;

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    public AdminServiceImpl(UserRepository userRepository,
                            DriverProfileRepository driverProfileRepository,
                            UserReportRepository userReportRepository,
                            UserFeedbackRepository userFeedbackRepository,
                            DriverProfileService driverProfileService,
                            PasswordEncoder passwordEncoder,
                            JwtUtil jwtUtil,
                            Environment environment) {
        this.userRepository = userRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.userReportRepository = userReportRepository;
        this.userFeedbackRepository = userFeedbackRepository;
        this.driverProfileService = driverProfileService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.environment = environment;
    }

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
            throw new ValidateRecordException("Access denied. Admin role required.", "errorMessage");
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
    @Transactional(readOnly = true)
    public List<DriverProfileResponse> getPendingDrivers() {
        log.info("Fetching pending driver profiles");
        return driverProfileRepository.findByAccountStatus("PENDING")
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
    public List<UserReportResponse> getAllReports(String status) {
        log.info("Fetching all reports, status filter: {}", status);
        List<com.ride.mate.domain.UserReport> reports;
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

        report.setStatus(request.getStatus().toUpperCase());
        report.setModifiedDate(DateUtil.getDate());
        report.setModifiedUser(LoginAuthentication.getUserName());
        UserReport updated = userReportRepository.save(report);

        log.info("Report status updated for ID: {}", reportId);
        return mapToReportResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserFeedbackResponse> getAllFeedback() {
        log.info("Fetching all user feedback");
        return userFeedbackRepository.findAllByOrderByCreatedDateDesc()
                .stream().map(this::mapToFeedbackResponse).collect(Collectors.toList());
    }

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

    private UserFeedbackResponse mapToFeedbackResponse(com.ride.mate.domain.UserFeedback f) {
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
}
