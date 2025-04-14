package com.rescueme.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // NEW_REQUEST, APPROVED_REQUEST
    private String message;

    private Long adopterId;
    private Long shelterId;

    private boolean read = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
