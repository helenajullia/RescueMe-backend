package com.rescueme.controller;

import com.rescueme.repository.entity.User;
import com.rescueme.service.ShelterProfileService;
import com.rescueme.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/shelters")
@RequiredArgsConstructor
public class ShelterProfileController {

    private final ShelterProfileService shelterProfileService;
    private final UserService userService;

    @GetMapping("/{shelterId}/profile")
    public ResponseEntity<Map<String, Object>> getShelterProfile(@PathVariable Long shelterId) {
        User shelter = userService.getShelterById(shelterId);

        // Create response with all necessary profile data
        Map<String, Object> response = new HashMap<>();
        response.put("id", shelter.getId());
        response.put("username", shelter.getUsername());
        response.put("email", shelter.getEmail());
        response.put("phoneNumber", shelter.getPhoneNumber());
        response.put("shelterType", shelter.getShelterType());
        response.put("county", shelter.getCounty());
        response.put("city", shelter.getCity());
        response.put("fullAddress", shelter.getFullAddress());
        response.put("zipCode", shelter.getZipCode());
        response.put("yearFounded", shelter.getYearFounded());
        response.put("hoursOfOperation", shelter.getHoursOfOperation());
        response.put("mission", shelter.getMission());
        response.put("status", shelter.getStatus());

        // Add document status information (boolean flags indicating if documents were uploaded)
        Map<String, Boolean> documentStatus = shelterProfileService.getDocumentStatus(shelterId);
        response.put("documents", documentStatus);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{shelterId}/profile/draft")
    public ResponseEntity<?> saveShelterProfileDraft(
            @PathVariable Long shelterId,
            @RequestBody Map<String, Object> profileData) {

        try {
            User updatedUser = shelterProfileService.saveShelterProfileDraft(shelterId, profileData);
            return ResponseEntity.ok(Collections.singletonMap("message", "Profile draft saved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Failed to save profile draft: " + e.getMessage()));
        }
    }

    @PatchMapping("/{shelterId}/profile/submit")
    public ResponseEntity<?> submitShelterProfile(
            @PathVariable Long shelterId,
            @RequestBody Map<String, Object> profileData) {

        try {
            User updatedUser = shelterProfileService.submitShelterProfile(shelterId, profileData);
            return ResponseEntity.ok(Collections.singletonMap("message", "Profile submitted successfully for review"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "Failed to submit profile: " + e.getMessage()));
        }
    }

//    @PostMapping("/{shelterId}/documents/{documentType}")
//    public ResponseEntity<?> uploadDocument(
//            @PathVariable Long shelterId,
//            @PathVariable String documentType,
//            @RequestParam("file") MultipartFile file) {
//
//        try {
//            shelterProfileService.uploadDocument(shelterId, documentType, file);
//            return ResponseEntity.ok(Collections.singletonMap("message", "Document uploaded successfully"));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.singletonMap("message", "Failed to upload document: " + e.getMessage()));
//        }
//    }

//    @GetMapping("/{shelterId}/documents/{documentType}")
//    public ResponseEntity<byte[]> getDocument(
//            @PathVariable Long shelterId,
//            @PathVariable String documentType) {
//
//        byte[] document = shelterProfileService.getDocument(shelterId, documentType);
//
//        if (document == null || document.length == 0) {
//            return ResponseEntity.notFound().build();
//        }
//
//        // Determine content type based on document type or extension
//        MediaType contentType = determineContentType(documentType, shelterProfileService.getDocumentContentType(shelterId, documentType));
//
//        return ResponseEntity.ok()
//                .contentType(contentType)
//                .body(document);
//    }

    private MediaType determineContentType(String documentType, String contentTypeStr) {
        if (contentTypeStr != null && !contentTypeStr.isEmpty()) {
            try {
                return MediaType.parseMediaType(contentTypeStr);
            } catch (Exception e) {
                // Log the error
                System.err.println("Error parsing media type: " + e.getMessage());
            }
        }

        // Default content types based on document extension
        if (contentTypeStr != null) {
            if (contentTypeStr.contains("pdf")) {
                return MediaType.APPLICATION_PDF;
            } else if (contentTypeStr.contains("jpeg") || contentTypeStr.contains("jpg")) {
                return MediaType.IMAGE_JPEG;
            } else if (contentTypeStr.contains("png")) {
                return MediaType.IMAGE_PNG;
            }
        }

        // Ultimate fallback
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    @GetMapping("/{shelterId}/check-welcome")
    public ResponseEntity<Map<String, Boolean>> checkWelcomeStatus(@PathVariable Long shelterId) {
        User shelter = userService.getShelterById(shelterId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("showWelcome", shelter.getFirstLoginAfterApproval() != null && shelter.getFirstLoginAfterApproval());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{shelterId}/acknowledge-welcome")
    public ResponseEntity<?> acknowledgeWelcome(@PathVariable Long shelterId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstLoginAfterApproval", false);
        userService.updateUser(shelterId, updates);

        return ResponseEntity.ok(Collections.singletonMap("message", "Welcome acknowledged"));
    }
}