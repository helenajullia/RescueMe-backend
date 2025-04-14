package com.rescueme.repository;

import com.rescueme.repository.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByAdopterId(Long adopterId);
    List<Notification> findByShelterId(Long shelterId);
}
