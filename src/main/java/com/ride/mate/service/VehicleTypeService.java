package com.ride.mate.service;

import com.ride.mate.domain.IdentificationType;
import com.ride.mate.domain.VehicleType;

import java.util.List;
import java.util.Optional;

public interface VehicleTypeService {

    /**
     * Get Vehicle Types passing by  id
     *
     * @param id -  Id
     * @return - JSON Array of VehicleTypes related  id
     *
     */

    public Optional<VehicleType> findById(long id);

    /**
     * Get Vehicle Types passing by  status
     *
     * @param status - status
     * @return - JSON Array of VehicleTypes related  status
     *
     */
    public List<VehicleType> findByStatus(String status);

}
