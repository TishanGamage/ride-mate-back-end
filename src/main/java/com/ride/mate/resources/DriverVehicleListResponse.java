package com.ride.mate.resources;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DriverVehicleListResponse
 * Response payload for driver vehicle list with multiple vehicle indicator
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 21-03-2026    N/A          N/A          Tishan          Initial Development
 */
@Getter
@Setter
@Builder
public class DriverVehicleListResponse {

    private Long driverProfileId;
    private boolean hasMultipleVehicles;
    private int totalVehicles;
    private List<DriverVehicleDetailsResponse> vehicles;
}
