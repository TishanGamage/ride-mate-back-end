package com.ride.mate.service.impl;
import com.ride.mate.core.LoginAuthentication;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.User;
import com.ride.mate.domain.UserReport;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.UserReportRepository;
import com.ride.mate.repository.UserRepository;
import com.ride.mate.resources.UserReportRequestResource;
import com.ride.mate.resources.UserReportResponse;
import com.ride.mate.service.UserReportService;
import com.ride.mate.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
@Transactional
public class UserReportServiceImpl extends MessagePropertyBase implements UserReportService {
    private static final String STATUS_PENDING = "PENDING";
    private final UserReportRepository userReportRepository;
    private final UserRepository userRepository;
    private final Environment environment;
    public UserReportServiceImpl(UserReportRepository userReportRepository,
                                  UserRepository userRepository,
                                  Environment environment) {
        this.userReportRepository = userReportRepository;
        this.userRepository = userRepository;
        this.environment = environment;
    }
    @Override
    public UserReportResponse submitReport(UserReportRequestResource resource) {
        log.info("Submitting report for user ID: {}, category: {}", resource.getUserId(), resource.getCategory());
        User user = userRepository.findById(resource.getUserId())
                .orElseThrow(() -> new ValidateRecordException(
                        environment.getProperty(RECORD_NOT_FOUND), "message"));
        UserReport report = new UserReport();
        report.setUser(user);
        report.setCategory(resource.getCategory());
        report.setSubject(resource.getSubject());
        report.setDescription(resource.getDescription());
        report.setStatus(STATUS_PENDING);
        report.setCreatedDate(DateUtil.getDate());
        report.setCreatedUser(LoginAuthentication.getUserName());
        report.setSyncTs(DateUtil.getDate());
        UserReport saved = userReportRepository.save(report);
        log.info("User report saved with ID: {}", saved.getId());
        return mapToResponse(saved);
    }
    @Override
    @Transactional(readOnly = true)
    public List<UserReportResponse> getReportsByUser(Long userId) {
        log.info("Fetching reports for user ID: {}", userId);
        return userReportRepository.findByUserIdOrderByCreatedDateDesc(userId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }
    private UserReportResponse mapToResponse(UserReport r) {
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
}
