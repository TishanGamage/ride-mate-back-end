package com.ride.mate.service.impl;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.IdentificationType;
import com.ride.mate.repository.IdentificationTypeRepository;
import com.ride.mate.service.IdentificationTypeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Identification Type Service Implementation
 * Business logic for managing identification types
 *
 * @author Iruni
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 23-02-2026    N/A          N/A          Iruni          Initial Development
 */
@Service
public class IdentificationTypeServiceImpl extends MessagePropertyBase implements IdentificationTypeService {

    private final IdentificationTypeRepository identificationTypeRepository;

    public IdentificationTypeServiceImpl(IdentificationTypeRepository identificationTypeRepository) {
        this.identificationTypeRepository = identificationTypeRepository;
    }

    @Override
    public Optional<IdentificationType> findById(long id) {
        return identificationTypeRepository.findById(id);
    }

    @Override
    public List<IdentificationType> findByStatus(String status) {
        return identificationTypeRepository.findByStatus(status);
    }
}
