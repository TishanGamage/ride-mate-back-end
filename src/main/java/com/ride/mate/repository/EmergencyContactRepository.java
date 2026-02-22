package com.ride.mate.repository;

import com.ride.mate.domain.EmergencyContact;
import com.ride.mate.domain.User;
import com.ride.mate.enums.YesNo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Emergency Contact Repository
 * Data access layer for emergency contact operations
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 22-02-2026    N/A          N/A          Tishan          Initial Development
 */
@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {

    List<EmergencyContact> findByUser(User user);

    List<EmergencyContact> findByUserId(Long userId);

    Optional<EmergencyContact> findByUserIdAndIsDefault(Long userId, YesNo isDefault);

    Optional<EmergencyContact> findByUserAndIsDefault(User user, YesNo isDefault);

    long countByUserId(Long userId);

    boolean existsByUserIdAndIsDefault(Long userId, YesNo isDefault);
}

