package com.rescueme.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private Long recipientId;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private boolean read = false;

    @Column(name = "conversation_id", nullable = false)
    private String conversationId;

    // Pre persist callback to set timestamp if not set
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }

        // Ensure the conversationId is created if not present
        if (conversationId == null || conversationId.isEmpty()) {
            // Create a consistent conversationId by sorting the user IDs
            long smallerId = Math.min(senderId, recipientId);
            long largerId = Math.max(senderId, recipientId);
            conversationId = smallerId + "_" + largerId;
        }
    }
}