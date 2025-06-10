package com.rescueme.controller;

import com.rescueme.repository.UserRepository;
import com.rescueme.repository.entity.ShelterStatus;
import com.rescueme.repository.entity.User;
import com.rescueme.service.AdminDashboardService;
import com.rescueme.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;
    private final UserService userService;

    /**
     * Returns the total number of shelters
     */
    @GetMapping("/shelters/count")
    public ResponseEntity<Long> getTotalSheltersCount() {
        return ResponseEntity.ok(adminDashboardService.getTotalSheltersCount());
    }

    /**
     * Returns the number of shelters that are pending admin approval
     */
    @GetMapping("/shelters/pending/count")
    public ResponseEntity<Long> getPendingSheltersCount() {
        return ResponseEntity.ok(adminDashboardService.getPendingSheltersCount());
    }

    /**
     * Returns the total number of users (both shelters and adopters)
     */
    @GetMapping("/users/count")
    public ResponseEntity<Long> getTotalUsersCount() {
        return ResponseEntity.ok(adminDashboardService.getTotalUsersCount());
    }

    /**
     * Returns the total number of pets listed
     */
    @GetMapping("/pets/count")
    public ResponseEntity<Long> getTotalPetsCount() {
        return ResponseEntity.ok(adminDashboardService.getTotalPetsCount());
    }

    /**
     * Returns a list of shelters that are waiting for admin approval
     */
    @GetMapping("/shelters/pending")
    public ResponseEntity<List<User>> getPendingShelters() {
        return ResponseEntity.ok(adminDashboardService.getPendingShelters());
    }

    /**
     * Returns a list of shelters that have already been approved
     */
    @GetMapping("/shelters/approved")
    public ResponseEntity<List<User>> getApprovedShelters() {
        return ResponseEntity.ok(adminDashboardService.getApprovedShelters());
    }

    /**
     * Approves a shelter by its ID
     * Updates the shelter's status to APPROVED, sets the approval time,
     * and flags firstLoginAfterApproval
     */
    @PostMapping("/shelters/{shelterId}/approve")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> approveShelter(@PathVariable Long shelterId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", ShelterStatus.APPROVED);
        updates.put("approvedAt", LocalDateTime.now());
        updates.put("firstLoginAfterApproval", true);

        userService.updateUser(shelterId, updates);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Shelter approved successfully");
        response.put("shelterId", shelterId);

        return ResponseEntity.ok(response);
    }

    /**
     * Rejects a shelter by its ID
     * Updates the shelter's status to REJECTED
     */
    @PostMapping("/shelters/{shelterId}/reject")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> rejectShelter(
            @PathVariable Long shelterId,
            @RequestBody Map<String, Object> rejectionData) {

        User shelter = userService.getShelterById(shelterId);

        String reason = (String) rejectionData.getOrDefault("reason", "UNSPECIFIED");
        String customReason = (String) rejectionData.get("customReason");
        String details = (String) rejectionData.get("details");

        // Build the full rejection reason to be stored
        String fullRejectionReason = reason;
        if ("OTHER".equals(reason) && customReason != null && !customReason.isEmpty()) {
            fullRejectionReason = customReason;
        }

        // Update shelter status and rejection details
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", ShelterStatus.REJECTED);
        updates.put("rejectionReason", fullRejectionReason);
        updates.put("rejectionDetails", details);
        updates.put("rejectedAt", LocalDateTime.now());

        userService.updateUser(shelterId, updates);

        return ResponseEntity.ok(Map.of("message", "Shelter rejected successfully"));
    }
}