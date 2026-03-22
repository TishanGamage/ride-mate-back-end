package com.ride.mate.repository;
import com.ride.mate.domain.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface UserReportRepository extends JpaRepository<UserReport, Long> {
    List<UserReport> findByUserIdOrderByCreatedDateDesc(Long userId);
}
