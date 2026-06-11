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

    /**
     * Returns the public and internal profile details of a shelter by ID
     * Also includes the status of required documents
     */
    @GetMapping("/{shelterId}/profile")
    public ResponseEntity<Map<String, Object>> getShelterProfile(@PathVariable Long shelterId) {
        User shelter = userService.getShelterById(shelterId);

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

        Map<String, Boolean> documentStatus = shelterProfileService.getDocumentStatus(shelterId);
        response.put("documents", documentStatus);

        return ResponseEntity.ok(response);
    }

    /**
     * Saves a draft version of the shelter's profile without submitting it for review
     */
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

    /**
     * Submits the final version of the shelter's profile for admin review
     */
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


    /**
     * Checks if the welcome modal should be shown after shelter approval
     */
    @GetMapping("/{shelterId}/check-welcome")
    public ResponseEntity<Map<String, Boolean>> checkWelcomeStatus(@PathVariable Long shelterId) {
        User shelter = userService.getShelterById(shelterId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("showWelcome", shelter.getFirstLoginAfterApproval() != null && shelter.getFirstLoginAfterApproval());

        return ResponseEntity.ok(response);
    }

    /**
     * Marks the welcome screen as acknowledged so it will not be shown again
     */
    @PostMapping("/{shelterId}/acknowledge-welcome")
    public ResponseEntity<?> acknowledgeWelcome(@PathVariable Long shelterId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstLoginAfterApproval", false);
        userService.updateUser(shelterId, updates);

        return ResponseEntity.ok(Collections.singletonMap("message", "Welcome acknowledged"));
    }
}