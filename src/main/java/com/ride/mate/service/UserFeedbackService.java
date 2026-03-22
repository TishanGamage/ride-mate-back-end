package com.ride.mate.service;
import com.ride.mate.resources.UserFeedbackRequestResource;
import com.ride.mate.resources.UserFeedbackResponse;
import java.util.List;
public interface UserFeedbackService {
    UserFeedbackResponse submitFeedback(UserFeedbackRequestResource resource);
    List<UserFeedbackResponse> getFeedbackByUser(Long userId);
}
