package com.ride.mate.service;
import com.ride.mate.resources.UserReportRequestResource;
import com.ride.mate.resources.UserReportResponse;
import java.util.List;
public interface UserReportService {
    UserReportResponse submitReport(UserReportRequestResource resource);
    List<UserReportResponse> getReportsByUser(Long userId);
}
