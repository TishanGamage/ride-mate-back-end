package com.ride.mate.service.impl;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.UserIdentificationDetails;
import com.ride.mate.repository.UserIdentificationDetailsRepository;
import com.ride.mate.service.UserIdentificationDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * User Identification Details Service Implementation
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 19-03-2026    N/A          N/A          Dulan          Initial Development
 */
@Slf4j
@Service
@Transactional
public class UserIdentificationDetailsServiceImpl extends MessagePropertyBase implements UserIdentificationDetailsService {

    private final UserIdentificationDetailsRepository userIdentificationDetailsRepository;

    public UserIdentificationDetailsServiceImpl(UserIdentificationDetailsRepository userIdentificationDetailsRepository) {
        this.userIdentificationDetailsRepository = userIdentificationDetailsRepository;
    }

    @Override
    public Optional<UserIdentificationDetails> findById(Long id) {
        log.info("Fetching user identification details by id: {}", id);
        return userIdentificationDetailsRepository.findById(id);
    }

    @Override
    public List<UserIdentificationDetails> findByUserId(Long userId) {
        log.info("Fetching user identification details by userId: {}", userId);
        return userIdentificationDetailsRepository.findByUserId(userId);
    }

    @Override
    public List<UserIdentificationDetails> findByUserIdAndStatus(Long userId, String status) {
        log.info("Fetching user identification details by userId: {} and status: {}", userId, status);
        return userIdentificationDetailsRepository.findByUserIdAndStatus(userId, status);
    }
}
