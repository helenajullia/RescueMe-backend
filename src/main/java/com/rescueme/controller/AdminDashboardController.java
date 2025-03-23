package com.rescueme.controller;

import com.rescueme.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

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
}