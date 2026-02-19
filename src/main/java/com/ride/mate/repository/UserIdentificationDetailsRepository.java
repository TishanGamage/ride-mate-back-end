package com.ride.mate.repository;

import com.ride.mate.domain.UserIdentificationDetails;
import com.ride.mate.enums.YesNo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * User Identification Details Repository
 * Data access layer for user identification operations
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 20-02-2026    N/A          N/A          Tishan          Initial Development
 */
@Repository
public interface UserIdentificationDetailsRepository extends JpaRepository<UserIdentificationDetails, Long> {

    List<UserIdentificationDetails> findByUserProfileId(Long userProfileId);

    Optional<UserIdentificationDetails> findByUserProfileIdAndIsPrimary(Long userProfileId, YesNo isPrimary);

    Optional<UserIdentificationDetails> findByIdentificationNumber(String identificationNumber);

    List<UserIdentificationDetails> findByUserProfileIdAndStatus(Long userProfileId, String status);
}

