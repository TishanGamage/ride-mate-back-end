package com.ride.mate.controller;

import com.ride.mate.core.MessagePropertyBase;
import com.ride.mate.domain.DocumentDetails;
import com.ride.mate.resources.SuccessAndErrorDetailsResource;
import com.ride.mate.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * File Controller
 * REST API endpoints for file upload and management
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
@Slf4j
@RestController
@RequestMapping(value = "/file")
@CrossOrigin(origins = "*")
public class FileController extends MessagePropertyBase {

    private final FileService fileService;
    private final Environment environment;

    public FileController(FileService fileService, Environment environment) {
        this.fileService = fileService;
        this.environment = environment;
    }

    /**
     * Upload a file
     * Accepts multipart file and saves document details to database
     *
     * @param file MultipartFile to upload
     * @return ResponseEntity with upload response containing document ID and details
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("Received file upload request for: {}", file.getOriginalFilename());
        DocumentDetails document = fileService.uploadFile(file);
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(document.getId());
        response.setMessages(environment.getProperty(FILE_UPLOADED_SUCCESS));
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get document details by ID
     *
     * @param id Document ID
     * @return ResponseEntity with document details
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDocumentById(@PathVariable Long id) {
        log.info("Received request to get document with ID: {}", id);

        DocumentDetails document = fileService.getDocumentById(id);
        if (document != null) {
            return new ResponseEntity<>(document, HttpStatus.OK);
        } else {
            SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
            response.setMessages(environment.getProperty(RECORD_NOT_FOUND));
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }
    }

    /**
     * View file inline by document ID (for img src usage)
     * Returns file bytes with Content-Type and inline Content-Disposition
     *
     * @param id Document ID
     * @return ResponseEntity with file bytes for inline display
     */
    @GetMapping("/view/{id}")
    public ResponseEntity<byte[]> viewFile(@PathVariable Long id) {
        log.info("Received request to view file with document ID: {}", id);

        DocumentDetails document = fileService.getDocumentById(id);
        byte[] fileContent = fileService.viewFile(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                document.getFileType() != null ? document.getFileType() : "application/octet-stream"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getDocumentName() + "\"");
        headers.setContentLength(fileContent.length);
        headers.setCacheControl("max-age=3600");

        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
    }

    /**
     * Download file by document ID
     * Returns the file content as a downloadable attachment
     *
     * @param id Document ID
     * @return ResponseEntity with file bytes and appropriate headers
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        log.info("Received request to download file with document ID: {}", id);

        // Get document details for filename and content type
        DocumentDetails document = fileService.getDocumentById(id);

        // Download file content
        byte[] fileContent = fileService.downloadFile(id);

        // Set response headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                document.getFileType() != null ? document.getFileType() : "application/octet-stream"));
        headers.setContentDispositionFormData("attachment", document.getDocumentName());
        headers.setContentLength(fileContent.length);

        log.info("File download successful for document ID: {}", id);
        return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
    }

    /**
     * Delete document by ID (soft delete)
     *
     * @param id Document ID
     * @return ResponseEntity with success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id) {
        log.info("Received request to delete document with ID: {}", id);
        fileService.deleteDocument(id);
        SuccessAndErrorDetailsResource response = new SuccessAndErrorDetailsResource();
        response.setId(id);
        response.setMessages(environment.getProperty(FILE_DELETED_SUCCESS));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
