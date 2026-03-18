package com.ride.mate.service;

import com.ride.mate.domain.UserProfile;
import com.ride.mate.resources.UserProfileAddResource;
import com.ride.mate.resources.UserProfileResponse;
import com.ride.mate.resources.UserProfileUpdateResource;
import com.ride.mate.resources.WillingToDriveUpdateResource;

/**
 * UserProfileService
 * Service interface for user profile management operations
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 15-03-2026    N/A          N/A          Tishan          Initial Development
 * 2 15-03-2026    N/A          N/A          Tishan          Added updateUserProfile method
 * 3 15-03-2026    N/A          N/A          Tishan          Added getUserProfileByUserId method
 * 4 16-03-2026    N/A          N/A          Tishan          Changed getUserProfileByUserId to return UserProfileResponse
 * 5 18-03-2026    N/A          N/A          Tishan          Added updateWillingToDrive method
 */
public interface UserProfileService {

    /**
     * Create a new user profile
     *
     * @param request user profile creation request
     * @return the created UserProfile entity
     */
    UserProfile createUserProfile(UserProfileAddResource request);

    /**
     * Update an existing user profile
     *
     * @param request user profile update request
     * @return the updated UserProfile entity
     */
    UserProfile updateUserProfile(UserProfileUpdateResource request, Long id);

    /**
     * Retrieve a user profile by user ID
     *
     * @param userId the ID of the user
     * @return UserProfileResponse containing full profile details
     */
    UserProfileResponse getUserProfileByUserId(Long userId);

    /**
     * Update only the willingToDrive field of a user profile
     *
     * @param id      the ID of the user profile record
     * @param request request containing the new willingToDrive value (YES/NO)
     * @return the updated UserProfile entity
     */
    UserProfile updateWillingToDrive(Long id, WillingToDriveUpdateResource request);
}

