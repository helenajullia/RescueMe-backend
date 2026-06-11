package com.rescueme.service;

import com.rescueme.repository.entity.Notification;

import java.util.List;

public interface NotificationService {
    void sendNotificationToShelter(Long shelterId, String message);
    void sendNotificationToAdopter(Long adopterId, String petName, boolean approved);
    List<Notification> getNotificationsForShelter(Long shelterId);
    List<Notification> getNotificationsForAdopter(Long adopterId);
    void markAsRead(Long notificationId);
    void sendToShelter(Long shelterId, Notification notification);
}