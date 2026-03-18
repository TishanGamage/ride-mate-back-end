package com.ride.mate.service.impl;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.VehicleMake;
import com.ride.mate.repository.VehicleMakeRepository;
import com.ride.mate.service.VehicleMakeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * VehicleMakeServiceImpl
 * Business logic implementation for vehicle make management
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 17-03-2026    N/A          N/A          Tishan          Initial Development
 */
@Slf4j
@Service
@Transactional
public class VehicleMakeServiceImpl extends MessagePropertyBase implements VehicleMakeService {

    private final VehicleMakeRepository vehicleMakeRepository;

    public VehicleMakeServiceImpl(VehicleMakeRepository vehicleMakeRepository) {
        this.vehicleMakeRepository = vehicleMakeRepository;
    }

    @Override
    public Optional<VehicleMake> findById(long id) {
        log.info("Fetching vehicle make by ID: {}", id);
        return vehicleMakeRepository.findById(id);
    }

    @Override
    public List<VehicleMake> findByStatus(String status) {
        log.info("Fetching vehicle makes by status: {}", status);
        return vehicleMakeRepository.findByStatus(status);
    }
}

