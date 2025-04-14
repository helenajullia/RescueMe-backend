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

    @PostMapping("/test/shelter")
    public void testShelterNotif(@RequestParam Long shelterId, @RequestParam String petName) {
        notificationService.sendNotificationToShelter(shelterId, petName);
    }

    @PostMapping("/test/adopter")
    public void testAdopterNotif(@RequestParam Long adopterId, @RequestParam String petName, @RequestParam boolean approved) {
        notificationService.sendNotificationToAdopter(adopterId, petName, approved);
    }

}
