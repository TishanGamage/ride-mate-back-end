package com.ride.mate.resources;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * UserIdentificationDetailsResponseResource
 * Response payload for user identification details retrieval
 *
 * @author Dulan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 22-03-2026    N/A          N/A          Dulan           Initial Development
 */
@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserIdentificationDetailsResponseResource {

    private Long id;
    private Long userId;
    private Long identificationTypeId;
    private String identificationTypeName;
    private String identificationNumber;
    private String issueDate;
    private String expiryDate;
    private String issuingCountry;
    private String issuingAuthority;

    // Document references (flat IDs for frontend AuthImage component)
    private Long frontImageDocumentId;
    private String frontImageDocumentUrl;
    private Long backImageDocumentId;
    private String backImageDocumentUrl;

    private String isVerified;
    private String verifiedDate;
    private String verifiedBy;
    private String verificationNotes;
    private String status;

    private String createdDate;
    private String modifiedDate;
}


