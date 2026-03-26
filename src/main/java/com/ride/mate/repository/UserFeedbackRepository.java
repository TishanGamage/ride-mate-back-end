package com.ride.mate.repository;
import com.ride.mate.domain.UserFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface UserFeedbackRepository extends JpaRepository<UserFeedback, Long> {
    List<UserFeedback> findByUserIdOrderByCreatedDateDesc(Long userId);
    List<UserFeedback> findAllByOrderByCreatedDateDesc();
}
