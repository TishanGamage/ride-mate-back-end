package com.ride.mate.service.impl;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.VehicleType;
import com.ride.mate.repository.VehicleTypeRepository;
import com.ride.mate.service.VehicleTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * VehicleTypeServiceImpl
 * Business logic implementation for vehicle type management
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 25-02-2026    N/A          N/A          Iruni           Initial Development
 * 2 17-03-2026    N/A          N/A          Tishan          Updated to follow coding standards
 */
@Slf4j
@Service
@Transactional
public class VehicleTypeServiceImpl extends MessagePropertyBase implements VehicleTypeService {

    private final VehicleTypeRepository vehicleTypeRepository;

    public VehicleTypeServiceImpl(VehicleTypeRepository vehicleTypeRepository) {
        this.vehicleTypeRepository = vehicleTypeRepository;
    }

    @Override
    public Optional<VehicleType> findById(long id) {
        log.info("Fetching vehicle type by ID: {}", id);
        return vehicleTypeRepository.findById(id);
    }

    @Override
    public List<VehicleType> findByStatus(String status) {
        log.info("Fetching vehicle types by status: {}", status);
        return vehicleTypeRepository.findByStatus(status);
    }
}
