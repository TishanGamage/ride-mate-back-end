package com.ride.mate.service.impl;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.UserIdentificationDetails;
import com.ride.mate.repository.UserIdentificationDetailsRepository;
import com.ride.mate.resources.UserIdentificationDetailsResponseResource;
import com.ride.mate.service.UserIdentificationDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * User Identification Details Service Implementation
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 19-03-2026    N/A          N/A          Dulan          Initial Development
 * 2 22-03-2026    N/A          N/A          Dulan          Added getResponseByUserId and entity-to-DTO conversion
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

    @Override
    public List<UserIdentificationDetailsResponseResource> getResponseByUserId(Long userId) {
        log.info("Fetching user identification details response for userId: {}", userId);
        List<UserIdentificationDetails> details = userIdentificationDetailsRepository.findByUserIdWithDocuments(userId);
        return details.stream()
                .map(this::toResponseResource)
                .collect(Collectors.toList());
    }

    /**
     * Convert UserIdentificationDetails entity to response DTO
     * Avoids returning raw entities with lazy relationships to prevent serialization issues
     *
     * @param entity the UserIdentificationDetails entity
     * @return UserIdentificationDetailsResponseResource DTO
     */
    private UserIdentificationDetailsResponseResource toResponseResource(UserIdentificationDetails entity) {
        return UserIdentificationDetailsResponseResource.builder()
                .id(entity.getId())
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .identificationTypeId(entity.getIdentificationType() != null ? entity.getIdentificationType().getId() : null)
                .identificationTypeName(entity.getIdentificationType() != null ? entity.getIdentificationType().getName() : null)
                .identificationNumber(entity.getIdentificationNumber())
                .issueDate(entity.getIssueDate() != null ? entity.getIssueDate().toString() : null)
                .expiryDate(entity.getExpiryDate() != null ? entity.getExpiryDate().toString() : null)
                .issuingCountry(entity.getIssuingCountry())
                .issuingAuthority(entity.getIssuingAuthority())
                .frontImageDocumentId(entity.getFrontImageDocument() != null ? entity.getFrontImageDocument().getId() : null)
                .frontImageDocumentUrl(entity.getFrontImageDocument() != null ? entity.getFrontImageDocument().getDocumentUrl() : null)
                .backImageDocumentId(entity.getBackImageDocument() != null ? entity.getBackImageDocument().getId() : null)
                .backImageDocumentUrl(entity.getBackImageDocument() != null ? entity.getBackImageDocument().getDocumentUrl() : null)
                .isVerified(entity.getIsVerified() != null ? entity.getIsVerified().name() : null)
                .verifiedDate(entity.getVerifiedDate() != null ? entity.getVerifiedDate().toString() : null)
                .verifiedBy(entity.getVerifiedBy())
                .verificationNotes(entity.getVerificationNotes())
                .status(entity.getStatus())
                .createdDate(entity.getCreatedDate() != null ? entity.getCreatedDate().toString() : null)
                .modifiedDate(entity.getModifiedDate() != null ? entity.getModifiedDate().toString() : null)
                .build();
    }
}
