package com.ride.mate.service.impl;

import com.ride.mate.core.LoginAuthentication;
import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.DocumentDetails;
import com.ride.mate.enums.DocumentStatus;
import com.ride.mate.exception.ValidateRecordException;
import com.ride.mate.repository.DocumentDetailsRepository;
import com.ride.mate.service.FileService;
import com.ride.mate.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * File Service Implementation
 * Handles file upload and document management business logic
 *
 * @author Tishan
 * @version 1.0.0
 * @since 1.0.0
 *
 * # Date       Story Point    Task No      Author           Description
 * ---------------------------------------------------------------------------
 * 1 02-03-2026    N/A          N/A          Tishan          Initial Development
 * 2 06-03-2026    N/A          N/A          Tishan          Supabase Storage Integration
 */
@Slf4j
@Service
@Transactional
public class FileServiceImpl extends MessagePropertyBase implements FileService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.storage.bucket}")
    private String bucketName;

    private final DocumentDetailsRepository documentDetailsRepository;
    private final Environment environment;
    private final RestTemplate restTemplate;

    public FileServiceImpl(DocumentDetailsRepository documentDetailsRepository, Environment environment) {
        this.documentDetailsRepository = documentDetailsRepository;
        this.environment = environment;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public DocumentDetails uploadFile(MultipartFile file) {
        log.info("Processing file upload for: {}", file.getOriginalFilename());

        // Validate file
        if (file.isEmpty()) {
            log.warn("Upload failed: File is empty");
            throw new ValidateRecordException(environment.getProperty(FILE_EMPTY), "errorMessage");
        }

        if (file.getOriginalFilename() == null || file.getOriginalFilename().isEmpty()) {
            log.warn("Upload failed: File name is empty");
            throw new ValidateRecordException(environment.getProperty(FILE_NAME_EMPTY), "errorMessage");
        }

        // Create document details entity with placeholder URL
        DocumentDetails document = new DocumentDetails();
        document.setDocumentName(file.getOriginalFilename());
        document.setDocumentUrl("PENDING_UPLOAD");
        document.setFileSize(file.getSize());
        document.setFileType(file.getContentType());
        document.setUploadDate(DateUtil.getDate());
        document.setStatus(DocumentStatus.ACTIVE);
        document.setCreatedDate(DateUtil.getDate());
        document.setCreatedUser(LoginAuthentication.getUserName());
        document.setSyncTs(DateUtil.getDate());

        // Save to database first to get the document ID
        documentDetailsRepository.save(document);
        log.info("Document metadata saved with ID: {}", document.getId());

        // Upload file to Supabase storage using document ID as filename
        String uploadedUrl = uploadToSupabase(file, document.getId());

        // Update document with actual URL
        document.setDocumentUrl(uploadedUrl);

        DocumentDetails updatedDocument = documentDetailsRepository.save(document);
        log.info("File uploaded successfully with document ID: {}", updatedDocument.getId());

        return updatedDocument;
    }

    /**
     * Upload file to Supabase storage
     *
     * @param file       MultipartFile to upload
     * @param documentId Document ID to use as filename
     * @return Public URL of the uploaded file
     */
    private String uploadToSupabase(MultipartFile file, Long documentId) {
        log.info("Uploading file to Supabase storage with document ID: {}", documentId);
        try {
            // Generate file path using document ID
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String filePath = documentId + fileExtension;
            // Build upload URL
            String uploadUrl = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucketName, filePath);
            log.info("Supabase upload URL: {}", uploadUrl);
            log.info("SupaBase Key: {}", supabaseKey);
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("apikey", supabaseKey);
            headers.set("x-upsert", "true");
            headers.setContentType(MediaType.parseMediaType(file.getContentType() != null ? file.getContentType() : "application/octet-stream"));

            // Create request entity with file bytes
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);
            // Execute upload request (PUT with upsert to overwrite if exists)
            ResponseEntity<String> response = restTemplate.exchange(uploadUrl, HttpMethod.PUT, requestEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                String publicUrl = getSupabasePublicUrl(filePath);
                log.info("File uploaded successfully to Supabase. URL: {}", publicUrl);
                return publicUrl;
            } else {
                log.error("Failed to upload file to Supabase. Status: {}", response.getStatusCode());
                throw new ValidateRecordException(
                        environment.getProperty(SUPABASE_UPLOAD_FAILED), "errorMessage");
            }
        } catch (IOException e) {
            log.error("Failed to read file bytes: {}", e.getMessage());
            throw new ValidateRecordException(
                    environment.getProperty(FILE_READ_ERROR), "errorMessage");
        } catch (RestClientException e) {
            log.error("Supabase API error: {}", e.getMessage());
            throw new ValidateRecordException(
                    environment.getProperty(SUPABASE_UPLOAD_FAILED), "errorMessage");
        }
    }

    /**
     * Get public URL for a file in Supabase storage
     *
     * @param filePath Path of the file in storage
     * @return Public URL of the file
     */
    private String getSupabasePublicUrl(String filePath) {
        return String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucketName, filePath);
    }

    /**
     * Extract file extension from filename
     *
     * @param filename Original filename
     * @return File extension including the dot (e.g., ".pdf")
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    @Override
    public DocumentDetails getDocumentById(Long documentId) {
        log.info("Retrieving document with ID: {}", documentId);
        DocumentDetails document = documentDetailsRepository.findById(documentId)
                .orElseThrow(() -> new ValidateRecordException(environment.getProperty(DOCUMENT_NOT_FOUND), "message"));
        log.info("Document retrieved successfully with ID: {}", documentId);
        return document;
    }

    @Override
    public byte[] downloadFile(Long documentId) {
        log.info("Processing file download for document ID: {}", documentId);
        // Get document details from database
        DocumentDetails document = documentDetailsRepository.findById(documentId)
                .orElseThrow(() -> new ValidateRecordException(environment.getProperty(DOCUMENT_NOT_FOUND), "message"));
        // Download file from Supabase
        byte[] fileContent = downloadFromSupabase(document);
        log.info("File downloaded successfully for document ID: {}", documentId);
        return fileContent;
    }

    /**
     * Download file from Supabase storage
     *
     * @param document DocumentDetails containing the file URL
     * @return byte array of the file content
     */
    private byte[] downloadFromSupabase(DocumentDetails document) {
        log.info("Downloading file from Supabase for document ID: {}", document.getId());
        try {
            // Extract file path from document URL or construct it
            String fileExtension = getFileExtension(document.getDocumentName());
            String filePath = document.getId() + fileExtension;
            // Build download URL (using authenticated endpoint for private buckets)
            String downloadUrl = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucketName, filePath);
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);
            headers.set("apikey", supabaseKey);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
            // Execute download request
            ResponseEntity<byte[]> response = restTemplate.exchange(downloadUrl, HttpMethod.GET, requestEntity, byte[].class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("File downloaded successfully from Supabase. Size: {} bytes", response.getBody().length);
                return response.getBody();
            } else {
                log.error("Failed to download file from Supabase. Status: {}", response.getStatusCode());
                throw new ValidateRecordException(environment.getProperty(SUPABASE_DOWNLOAD_FAILED), "errorMessage");
            }
        } catch (RestClientException e) {
            log.error("Supabase API error during download: {}", e.getMessage());
            throw new ValidateRecordException(environment.getProperty(SUPABASE_DOWNLOAD_FAILED), "errorMessage");
        }
    }

    @Override
    public void deleteDocument(Long documentId) {
        log.info("Processing document deletion for ID: {}", documentId);
        DocumentDetails document = documentDetailsRepository.findById(documentId).orElseThrow(() -> new ValidateRecordException(environment.getProperty(DOCUMENT_NOT_FOUND), "message"));
        document.setStatus(DocumentStatus.INACTIVE);
        document.setModifiedDate(DateUtil.getDate());
        document.setModifiedUser(LoginAuthentication.getUserName());
        documentDetailsRepository.save(document);
        log.info("Document deleted successfully with ID: {}", documentId);
    }
}

