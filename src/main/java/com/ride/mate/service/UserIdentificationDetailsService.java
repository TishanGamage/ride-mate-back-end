package com.ride.mate.service;

import com.ride.mate.domain.UserIdentificationDetails;

import java.util.List;
import java.util.Optional;

/**
 * User Identification Details Service Interface
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 19-03-2026    N/A          N/A          Dulan          Initial Development
 */
public interface UserIdentificationDetailsService {

    Optional<UserIdentificationDetails> findById(Long id);

    List<UserIdentificationDetails> findByUserId(Long userId);

    List<UserIdentificationDetails> findByUserIdAndStatus(Long userId, String status);
}
