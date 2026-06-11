package com.rescueme.controller;

import com.rescueme.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-notifications")
@RequiredArgsConstructor
public class TestNotificationController {

    private final NotificationService notificationService;

    /**
     * Sends a test notification to a shelter about a new adoption request
     * Used for testing WebSocket shelter notifications
     */
    @PostMapping("/test/shelter")
    public void testShelterNotif(@RequestParam Long shelterId, @RequestParam String petName) {
        notificationService.sendNotificationToShelter(shelterId, petName);
    }

    /**
     * Sends a test notification to an adopter about their adoption status
     * Used for testing WebSocket adopter notifications
     */
    @PostMapping("/test/adopter")
    public void testAdopterNotif(@RequestParam Long adopterId, @RequestParam String petName, @RequestParam boolean approved) {
        notificationService.sendNotificationToAdopter(adopterId, petName, approved);
    }

}
