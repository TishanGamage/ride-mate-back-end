package com.ride.mate.repository;

import com.ride.mate.domain.UserSavedCard;
import com.ride.mate.enums.YesNo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserSavedCardRepository
 * Data access layer for UserSavedCard entity
 *
 * @author Danushka
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-03-2026    N/A          N/A          Danushka          Initial Development
 */
@Repository
public interface UserSavedCardRepository extends JpaRepository<UserSavedCard, Long> {

    List<UserSavedCard> findByUserId(Long userId);

    List<UserSavedCard> findByUserIdAndIsActive(Long userId, YesNo isActive);

    Optional<UserSavedCard> findByUserIdAndIsActiveOrderByIdDesc(Long userId, YesNo isActive);

    boolean existsByUserIdAndCustomerToken(Long userId, String customerToken);
}

