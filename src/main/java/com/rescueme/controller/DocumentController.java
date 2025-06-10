package com.rescueme.controller;

import com.rescueme.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/shelters")
@RequiredArgsConstructor
@Slf4j
public class DocumentController {

    private final DocumentService documentService;

    /**
     * Uploads a document for the specified shelter and document type
     */
    @PostMapping("/{shelterId}/documents/{documentType}")
    @PreAuthorize("hasRole('ROLE_SHELTER')")
    public ResponseEntity<?> uploadDocument(
            @PathVariable Long shelterId,
            @PathVariable String documentType,
            @RequestParam("file") MultipartFile file) {

        try {
            log.info("Document upload request received for shelter: {} and type: {}",
                    shelterId, documentType);

            documentService.uploadDocument(shelterId, documentType, file);

            log.info("Document successfully uploaded for shelter: {} and type: {}",
                    shelterId, documentType);

            return ResponseEntity.ok(Collections.singletonMap("message", "Document successfully uploaded"));
        } catch (ResponseStatusException e) {
            log.warn("Error uploading document: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode())
                    .body(Collections.singletonMap("message", e.getReason()));
        } catch (Exception e) {
            log.error("Unexpected error occurred while uploading the document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Error uploading document: " + e.getMessage()));
        }
    }

    /**
     * Retrieves a specific document for the given shelter and document type
     */
    @GetMapping("/{shelterId}/documents/{documentType}")
    public ResponseEntity<byte[]> getDocument(
            @PathVariable Long shelterId,
            @PathVariable String documentType) {

        try {
            log.info("Document retrieval request received for shelter: {} and type: {}",
                    shelterId, documentType);

            byte[] document = documentService.getDocument(shelterId, documentType);
            String contentType = documentService.getDocumentContentType(shelterId, documentType);

            MediaType mediaType = determineMediaType(contentType);

            log.info("Document successfully delivered for shelter: {} and type: {}",
                    shelterId, documentType);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(document);
        } catch (ResponseStatusException e) {
            log.warn("Error retrieving the document: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            log.error("Unexpected error occurred while retrieving the document", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * Deletes a document for the given shelter and document type
     */
    @DeleteMapping("/{shelterId}/documents/{documentType}")
    @PreAuthorize("hasRole('ROLE_SHELTER')")
    public ResponseEntity<?> deleteDocument(
            @PathVariable Long shelterId,
            @PathVariable String documentType) {

        try {
            documentService.deleteDocument(shelterId, documentType);
            return ResponseEntity.ok(Collections.singletonMap("message", "Document deleted successfully"));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(Collections.singletonMap("message", e.getReason()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Failed to delete document: " + e.getMessage()));
        }
    }


    /**
     * Returns the status of required documents for a given shelter
     * Each entry in the map shows whether a document type has been uploaded
     */
    @GetMapping("/{shelterId}/documents/status")
    public ResponseEntity<Map<String, Boolean>> getDocumentStatus(@PathVariable Long shelterId) {
        try {
            log.info("Document status request received for shelter: {}", shelterId);

            Map<String, Boolean> status = documentService.getDocumentStatus(shelterId);

            log.info("Document status successfully retrieved for shelter: {}", shelterId);

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error retrieving document status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Safely parses the content type string to a MediaType
     * Returns application/octet-stream as fallback
     */
    private MediaType determineMediaType(String contentType) {
        try {
            if (contentType != null && !contentType.isEmpty()) {
                return MediaType.parseMediaType(contentType);
            }
        } catch (Exception e) {
            log.warn("Error parsing media type: {}", e.getMessage());
        }

        return MediaType.APPLICATION_OCTET_STREAM;
    }
}