package com.ride.mate.repository;

import com.ride.mate.domain.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Vehicle Model Repository
 * Data access layer for vehicle model operations
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 18-03-2026    N/A          N/A          Iruni           Initial Development
 */
@Repository
public interface VehicleModelRepository extends JpaRepository<VehicleModel, Long> {

    /**
     * Find vehicle models by vehicle make id and status
     *
     * @param vehicleMakeId - Vehicle Make Id
     * @param status - Status
     * @return List of VehicleModel records matching the criteria
     */
    List<VehicleModel> findByVehicleMakeIdAndStatus(Long vehicleMakeId, String status);
}

