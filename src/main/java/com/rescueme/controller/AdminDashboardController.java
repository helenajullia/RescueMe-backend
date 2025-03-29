package com.rescueme.controller;

import com.rescueme.repository.UserRepository;
import com.rescueme.repository.entity.ShelterStatus;
import com.rescueme.repository.entity.User;
import com.rescueme.service.AdminDashboardService;
import com.rescueme.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    private final UserRepository userRepository;

    @GetMapping("/shelters/count")
    public ResponseEntity<Long> getTotalSheltersCount() {
        return ResponseEntity.ok(adminDashboardService.getTotalSheltersCount());
    }

    @GetMapping("/shelters/pending/count")
    public ResponseEntity<Long> getPendingSheltersCount() {
        return ResponseEntity.ok(adminDashboardService.getPendingSheltersCount());
    }

    @GetMapping("/users/count")
    public ResponseEntity<Long> getTotalUsersCount() {
        return ResponseEntity.ok(adminDashboardService.getTotalUsersCount());
    }

    @GetMapping("/pets/count")
    public ResponseEntity<Long> getTotalPetsCount() {
        return ResponseEntity.ok(adminDashboardService.getTotalPetsCount());
    }

    @GetMapping("/shelters/pending")
    public ResponseEntity<List<User>> getPendingShelters() {
        return ResponseEntity.ok(adminDashboardService.getPendingShelters());
    }

    @GetMapping("/shelters/approved")
    public ResponseEntity<List<User>> getApprovedShelters() {
        return ResponseEntity.ok(adminDashboardService.getApprovedShelters());
    }

    @PostMapping("/shelters/{shelterId}/approve")
    public ResponseEntity<Map<String, Object>> approveShelter(@PathVariable Long shelterId) {
        User shelter = userService.getShelterById(shelterId);

        // Create a map of updates
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

    @PostMapping("/shelters/{shelterId}/reject")
    public ResponseEntity<Map<String, String>> rejectShelter(@PathVariable Long shelterId) {
        User shelter = userService.getShelterById(shelterId);

        // Update shelter status to rejected
        shelter.setStatus(ShelterStatus.REJECTED);
        userService.updateUser(shelterId, Map.of("status", ShelterStatus.REJECTED));

        return ResponseEntity.ok(Map.of("message", "Shelter rejected successfully"));
    }



}