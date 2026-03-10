package com.ride.mate.repository;

import com.ride.mate.domain.DocumentDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Document Details Repository
 * Data access layer for document_details table
 * This is a standalone document storage - other tables reference documents by ID
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 02-03-2026    N/A          N/A          Tishan          Initial Development
 */
@Repository
public interface DocumentDetailsRepository extends JpaRepository<DocumentDetails, Long> {

    /**
     * Find documents by status
     *
     * @param status Status
     * @return List of document details
     */
    List<DocumentDetails> findByStatus(String status);

    /**
     * Find document by document name
     *
     * @param documentName Document name
     * @return Optional of document details
     */
    Optional<DocumentDetails> findByDocumentName(String documentName);

    /**
     * Check if document exists by document name
     *
     * @param documentName Document name
     * @return true if exists, false otherwise
     */
    boolean existsByDocumentName(String documentName);
}

