package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.RideDetail;
import com.ride.mate.service.IdentificationTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RideDetail Controller
 * REST API endpoints for RideDetails

 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 15-03-2026    N/A          N/A          Iruni          Initial Development
 */

@RestController
@RequestMapping(value = "/ride-details")
@CrossOrigin(origins = "*")
public class RideDetailController extends MessagePropertyBase {

    @Autowired
    private RideDetailService rideDetailService;




}
