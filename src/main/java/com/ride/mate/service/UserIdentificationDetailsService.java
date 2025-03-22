package com.ride.mate.service;

import com.ride.mate.domain.UserIdentificationDetails;
import com.ride.mate.resources.UserIdentificationDetailsResponseResource;

import java.util.List;
import java.util.Optional;

/**
 * User Identification Details Service Interface
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 19-03-2026    N/A          N/A          Dulan          Initial Development
 * 2 22-03-2026    N/A          N/A          Dulan          Added getResponseByUserId method
 */
public interface UserIdentificationDetailsService {

    Optional<UserIdentificationDetails> findById(Long id);

    List<UserIdentificationDetails> findByUserId(Long userId);

    List<UserIdentificationDetails> findByUserIdAndStatus(Long userId, String status);

    /**
     * Get identification details response DTOs by user ID
     *
     * @param userId the ID of the user
     * @return list of UserIdentificationDetailsResponseResource DTOs
     */
    List<UserIdentificationDetailsResponseResource> getResponseByUserId(Long userId);
}
