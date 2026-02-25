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
 * Vehicle Type Service Implementation
 * Business logic for managing vehicle types
 *
 * @author Iruni
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 25-02-2026    N/A          N/A          Iruni          Initial Development
 */
@Slf4j
@Service
@Transactional
public class VehicleTypeServiceImpl  extends MessagePropertyBase implements VehicleTypeService {


    private final VehicleTypeRepository vehicleTypeRepository;

    public VehicleTypeServiceImpl(VehicleTypeRepository vehicleTypeRepository) {
        this.vehicleTypeRepository = vehicleTypeRepository;
    }

    @Override
    public Optional<VehicleType> findById(long id) {
        return vehicleTypeRepository.findById(id);
    }

    @Override
    public List<VehicleType> findByStatus(String status) {
        return vehicleTypeRepository.findByStatus(status);
    }
}
