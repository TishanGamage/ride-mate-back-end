package com.ride.mate.service;

import com.ride.mate.domain.DocumentDetails;
import org.springframework.web.multipart.MultipartFile;

/**
 * File Service Interface
 * Business logic interface for file upload operations
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 02-03-2026    N/A          N/A          Tishan          Initial Development
 * 2 06-03-2026    N/A          N/A          Tishan          Download File API
 */
public interface FileService {

    /**
     * Upload a file and save document details
     *
     * @param file MultipartFile to upload
     * @return DocumentDetails entity with saved information
     */
    DocumentDetails uploadFile(MultipartFile file);

    /**
     * Get document details by ID
     *
     * @param documentId Document ID
     * @return DocumentDetails entity
     */
    DocumentDetails getDocumentById(Long documentId);

    /**
     * Download file from Supabase storage by document ID
     *
     * @param documentId Document ID
     * @return byte array of the file content
     */
    byte[] downloadFile(Long documentId);

    /**
     * Delete document by ID (soft delete)
     *
     * @param documentId Document ID
     */
    void deleteDocument(Long documentId);
}

