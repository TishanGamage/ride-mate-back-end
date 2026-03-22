package com.ride.mate.service.impl;
import com.ride.mate.core.LoginAuthentication;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.User;
import com.ride.mate.domain.UserFeedback;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.UserFeedbackRepository;
import com.ride.mate.repository.UserRepository;
import com.ride.mate.resources.UserFeedbackRequestResource;
import com.ride.mate.resources.UserFeedbackResponse;
import com.ride.mate.service.UserFeedbackService;
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
public class UserFeedbackServiceImpl extends MessagePropertyBase implements UserFeedbackService {
    private final UserFeedbackRepository userFeedbackRepository;
    private final UserRepository userRepository;
    private final Environment environment;
    public UserFeedbackServiceImpl(UserFeedbackRepository userFeedbackRepository,
                                    UserRepository userRepository,
                                    Environment environment) {
        this.userFeedbackRepository = userFeedbackRepository;
        this.userRepository = userRepository;
        this.environment = environment;
    }
    @Override
    public UserFeedbackResponse submitFeedback(UserFeedbackRequestResource resource) {
        log.info("Submitting feedback for user ID: {}, rating: {}", resource.getUserId(), resource.getRating());
        User user = userRepository.findById(resource.getUserId())
                .orElseThrow(() -> new ValidateRecordException(
                        environment.getProperty(RECORD_NOT_FOUND), "message"));
        UserFeedback feedback = new UserFeedback();
        feedback.setUser(user);
        feedback.setRating(resource.getRating());
        feedback.setCategory(resource.getCategory());
        feedback.setFeedbackText(resource.getFeedbackText());
        feedback.setCreatedDate(DateUtil.getDate());
        feedback.setCreatedUser(LoginAuthentication.getUserName());
        feedback.setSyncTs(DateUtil.getDate());
        UserFeedback saved = userFeedbackRepository.save(feedback);
        log.info("User feedback saved with ID: {}", saved.getId());
        return mapToResponse(saved);
    }
    @Override
    @Transactional(readOnly = true)
    public List<UserFeedbackResponse> getFeedbackByUser(Long userId) {
        log.info("Fetching feedback for user ID: {}", userId);
        return userFeedbackRepository.findByUserIdOrderByCreatedDateDesc(userId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }
    private UserFeedbackResponse mapToResponse(UserFeedback f) {
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
