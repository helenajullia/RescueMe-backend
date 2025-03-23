package com.rescueme.controller;

import com.rescueme.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
     * Încarcă un document pentru un adăpost
     */
    @PostMapping("/{shelterId}/documents/{documentType}")
    public ResponseEntity<?> uploadDocument(
            @PathVariable Long shelterId,
            @PathVariable String documentType,
            @RequestParam("file") MultipartFile file) {

        try {
            log.info("Cerere de încărcare document primită pentru adăpostul: {} și tipul: {}",
                    shelterId, documentType);

            documentService.uploadDocument(shelterId, documentType, file);

            log.info("Document încărcat cu succes pentru adăpostul: {} și tipul: {}",
                    shelterId, documentType);

            return ResponseEntity.ok(Collections.singletonMap("message", "Document încărcat cu succes"));
        } catch (ResponseStatusException e) {
            log.warn("Eroare la încărcarea documentului: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode())
                    .body(Collections.singletonMap("message", e.getReason()));
        } catch (Exception e) {
            log.error("Eroare neașteptată la încărcarea documentului", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Eroare la încărcarea documentului: " + e.getMessage()));
        }
    }

    /**
     * Obține un document pentru un adăpost
     */
    @GetMapping("/{shelterId}/documents/{documentType}")
    public ResponseEntity<byte[]> getDocument(
            @PathVariable Long shelterId,
            @PathVariable String documentType) {

        try {
            log.info("Cerere de obținere document primită pentru adăpostul: {} și tipul: {}",
                    shelterId, documentType);

            byte[] document = documentService.getDocument(shelterId, documentType);
            String contentType = documentService.getDocumentContentType(shelterId, documentType);

            // Determină MediaType în funcție de contentType
            MediaType mediaType = determineMediaType(contentType);

            log.info("Document livrat cu succes pentru adăpostul: {} și tipul: {}",
                    shelterId, documentType);

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(document);
        } catch (ResponseStatusException e) {
            log.warn("Eroare la obținerea documentului: {}", e.getReason());
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            log.error("Eroare neașteptată la obținerea documentului", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Șterge un document
     */
    @DeleteMapping("/{shelterId}/documents/{documentType}")
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
     * Obține statusul documentelor pentru un adăpost
     */
    @GetMapping("/{shelterId}/documents/status")
    public ResponseEntity<Map<String, Boolean>> getDocumentStatus(@PathVariable Long shelterId) {
        try {
            log.info("Cerere de obținere status document primită pentru adăpostul: {}", shelterId);

            Map<String, Boolean> status = documentService.getDocumentStatus(shelterId);

            log.info("Status document obținut cu succes pentru adăpostul: {}", shelterId);

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Eroare la obținerea statusului documentelor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Determină MediaType bazat pe content type
     */
    private MediaType determineMediaType(String contentType) {
        try {
            if (contentType != null && !contentType.isEmpty()) {
                return MediaType.parseMediaType(contentType);
            }
        } catch (Exception e) {
            log.warn("Eroare la parsarea tipului de media: {}", e.getMessage());
        }

        // Fallback la application/octet-stream
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}