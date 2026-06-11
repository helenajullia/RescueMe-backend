package com.rescueme.controller;

import com.rescueme.repository.entity.Notification;
import com.rescueme.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Returns the list of notifications for a specific shelter
     */
    @GetMapping("/shelter/{shelterId}")
    public List<Notification> getForShelter(@PathVariable Long shelterId) {
        return notificationService.getNotificationsForShelter(shelterId);
    }

    /**
     * Returns the list of notifications for a specific adopter
     */
    @GetMapping("/adopter/{adopterId}")
    public List<Notification> getForAdopter(@PathVariable Long adopterId) {
        return notificationService.getNotificationsForAdopter(adopterId);
    }

    /**
     * Marks a specific notification as read by its ID
     */
    @PostMapping("/{id}/mark-read")
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
    }
}
