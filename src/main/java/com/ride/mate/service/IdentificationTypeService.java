package com.ride.mate.service;

import com.ride.mate.domain.IdentificationType;

import java.util.List;
import java.util.Optional;

/**
 * Identification Type Service Interface
    * Business logic for managing identification types

 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 23-02-2026    N/A          N/A          Iruni          Initial Development
 */
public interface IdentificationTypeService {

    /**
     * Get Identifcation Types passing by  id
     *
     * @param id -  Id
     * @return - JSON Array of identificationTypes related  id
     *
     */

    public Optional<IdentificationType> findById(long id);

    /**
     * Get Identifcation Types passing by  status
     *
     * @param status - status
     * @return - JSON Array of identificationTypes related  status
     *
     */
    public List<IdentificationType> findByStatus(String status);





}
