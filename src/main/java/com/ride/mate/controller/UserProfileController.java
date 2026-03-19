package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.UserProfile;
import com.ride.mate.resources.SuccessAndErrorDetailsResource;
import com.ride.mate.resources.ProfilePhotoUpdateResource;
import com.ride.mate.resources.UserProfileAddResource;
import com.ride.mate.resources.UserProfileResponse;
import com.ride.mate.resources.UserProfileUpdateResource;
import com.ride.mate.resources.WillingToDriveUpdateResource;
import com.ride.mate.resources.UpdateRoleRequest;
import com.ride.mate.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * UserProfileController
 * REST API endpoints for user profile management operations
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 15-03-2026    N/A          N/A          Tishan          Initial Development
 * 2 15-03-2026    N/A          N/A          Tishan          Added updateUserProfile endpoint
 * 3 15-03-2026    N/A          N/A          Tishan          Added getUserProfileByUserId endpoint
 * 4 16-03-2026    N/A          N/A          Tishan          Changed getUserProfileByUserId to return UserProfileResponse
 * 5 18-03-2026    N/A          N/A          Tishan          Added updateWillingToDrive endpoint
 * 6 19-03-2026    N/A          N/A          Tishan          Added updateProfilePhoto endpoint
 */
@Slf4j
@RestController
@RequestMapping(value = "/user-profile")
@CrossOrigin(origins = "*")
public class UserProfileController extends MessagePropertyBase {

    private final UserProfileService userProfileService;
    private final Environment environment;

    public UserProfileController(UserProfileService userProfileService, Environment environment) {
        this.userProfileService = userProfileService;
        this.environment = environment;
    }

    /**
     * Create a new user profile
     * Creates a profile record linked to an existing user
     *
     * @param request user profile creation request containing profile details
     * @return ResponseEntity with created user profile ID and success message
     */
    @PostMapping(value = "/create")
    public ResponseEntity<?> createUserProfile(@Valid @RequestBody UserProfileAddResource request) {
        log.info("Received user profile creation request for user ID: {}", request.getUserId());
        UserProfile userProfile = userProfileService.createUserProfile(request);
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(userProfile.getId());
        response.setMessages(environment.getProperty(RECORD_CREATED));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Update an existing user profile
     * Updates profile fields and optionally identification and emergency contact details
     *
     * @param request user profile update request containing profile details
     * @return ResponseEntity with updated user profile ID and success message
     */
    @PutMapping(value = "/update/{id}")
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody UserProfileUpdateResource request
            , @PathVariable Long id) {
        UserProfile userProfile = userProfileService.updateUserProfile(request,id);
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(userProfile.getId());
        response.setMessages(environment.getProperty(RECORD_UPDATED));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get user profile by user ID
     * Retrieves the full profile details for the given user
     *
     * @param userId the ID of the user
     * @return ResponseEntity with UserProfileResponse containing full profile details
     */
    @GetMapping(value = "/user/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfileByUserId(@PathVariable Long userId) {
        log.info("Received get user profile request for user ID: {}", userId);
        UserProfileResponse response = userProfileService.getUserProfileByUserId(userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Update willingToDrive field of a user profile
     * Accepts YES or NO and updates only the willingToDrive column
     *
     * @param userId  the ID of the user
     * @param request request body containing the willingToDrive value (YES/NO)
     * @return ResponseEntity with updated user profile ID and success message
     */
    @PatchMapping(value = "/update-willing-to-drive/{userId}")
    public ResponseEntity<?> updateWillingToDrive(@PathVariable Long userId,
                                                  @Valid @RequestBody WillingToDriveUpdateResource request) {
        log.info("Received willingToDrive update request for user profile ID: {}", userId);
        UserProfile userProfile = userProfileService.updateWillingToDrive(userId, request);
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(userProfile.getId());
        response.setMessages(environment.getProperty(RECORD_UPDATED));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Update profile photo of a user profile
     * Updates only the profileImageDocument field for the given user
     *
     * @param userId  the ID of the user
     * @param request request body containing the new profileImageDocumentId
     * @return ResponseEntity with updated user profile ID and success message
     */
    @PutMapping(value = "/update-profile-photo/{userId}")
    public ResponseEntity<?> updateProfilePhoto(@PathVariable Long userId,
                                                @Valid @RequestBody ProfilePhotoUpdateResource request) {
        log.info("Received profile photo update request for user ID: {}", userId);
        UserProfile userProfile = userProfileService.updateProfilePhoto(userId, request);
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(userProfile.getId());
        response.setMessages(environment.getProperty(PROFILE_PHOTO_UPDATED));
        if (userProfile.getProfileImageDocument() != null) {
            response.setUrl(userProfile.getProfileImageDocument().getDocumentUrl());
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping(value = "/update-role/{userId}")
    public ResponseEntity<SuccessAndErrorDetailsResource> updateRole(@PathVariable Long userId,
                                                                     @Valid @RequestBody UpdateRoleRequest request) {
        log.info("Received role update request for user ID: {}", userId);
        SuccessAndErrorDetailsResource response = userProfileService.updateRole(userId, request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}