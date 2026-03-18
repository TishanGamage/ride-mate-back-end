package com.ride.mate.service.impl;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.VehicleModel;
import com.ride.mate.repository.VehicleModelRepository;
import com.ride.mate.service.VehicleModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * VehicleModelServiceImpl
 * Business logic implementation for vehicle model management
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-03-2026    N/A          N/A          Iruni           Initial Development
 */
@Slf4j
@Service
@Transactional
public class VehicleModelServiceImpl extends MessagePropertyBase implements VehicleModelService {

    private final VehicleModelRepository vehicleModelRepository;

    public VehicleModelServiceImpl(VehicleModelRepository vehicleModelRepository) {
        this.vehicleModelRepository = vehicleModelRepository;
    }

    @Override
    public List<VehicleModel> findByVehicleMakeIdAndStatus(Long vehicleMakeId, String status) {
        log.info("Fetching vehicle models by vehicle make ID: {} and status: {}", vehicleMakeId, status);
        return vehicleModelRepository.findByVehicleMakeIdAndStatus(vehicleMakeId, status);
    }
}

