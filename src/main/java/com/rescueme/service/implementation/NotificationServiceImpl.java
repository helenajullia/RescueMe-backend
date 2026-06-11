package com.rescueme.service.implementation;

import com.rescueme.repository.NotificationRepository;
import com.rescueme.repository.entity.Notification;
import com.rescueme.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;


    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void sendNotificationToShelter(Long shelterId, String petName) {
        String message = "New adoption request received for " + petName;
        Notification notification = new Notification(null, "NEW_ADOPTION_REQUEST", message, null, shelterId, false, LocalDateTime.now());
        notificationRepository.save(notification);

        messagingTemplate.convertAndSend("/topic/shelter/" + shelterId, notification);
    }

    @Override
    public void sendNotificationToAdopter(Long adopterId, String petName, boolean approved) {
        String status = approved ? "approved" : "rejected";
        String message = "Your adoption request for " + petName + " has been " + status;
        Notification notification = new Notification(null, "APPROVED_REQUEST", message, adopterId, null, false, LocalDateTime.now());
        notificationRepository.save(notification);

        messagingTemplate.convertAndSend("/topic/adopter/" + adopterId, notification);
    }



    @Override
    public List<Notification> getNotificationsForShelter(Long shelterId) {
        return notificationRepository.findByShelterId(shelterId);
    }

    @Override
    public List<Notification> getNotificationsForAdopter(Long adopterId) {
        return notificationRepository.findByAdopterId(adopterId);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow();
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void sendToShelter(Long shelterId, Notification notification) {
        messagingTemplate.convertAndSend("/topic/shelter/" + shelterId, notification);
    }
}
