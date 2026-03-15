package com.ride.mate.service;

import com.ride.mate.domain.RideDetail;
import com.ride.mate.resources.RideDetailRequestResource;

/**
 * Ride Detail Service Interface
 * Business logic for managing ride details
 *
 * @author Iruni
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 15-03-2026    N/A          N/A          Iruni           Initial Development
 */
public interface RideDetailService {

    /**
     * Create a new ride detail
     *
     * @param request Ride detail request resource
     * @return Created RideDetail entity
     */
    RideDetail createRideDetail(RideDetailRequestResource request);
}

