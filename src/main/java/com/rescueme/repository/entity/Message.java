package com.rescueme.repository.entity;

import com.rescueme.repository.dto.MessageDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageDTO.MessageType type = MessageDTO.MessageType.TEXT;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "messageId")
    private List<MessageAttachment> attachments = new ArrayList<>();


    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }

        if (conversationId == null || conversationId.isEmpty()) {
            long smallerId = Math.min(senderId, recipientId);
            long largerId = Math.max(senderId, recipientId);
            conversationId = smallerId + "_" + largerId;
        }

        if (!attachments.isEmpty()) {
            boolean hasImages = attachments.stream()
                    .anyMatch(a -> a.getContentType().startsWith("image/"));
            boolean hasDocs = attachments.stream()
                    .anyMatch(a -> !a.getContentType().startsWith("image/"));

            if (hasImages && hasDocs) {
                this.type = MessageDTO.MessageType.MIXED;
            } else if (hasImages) {
                this.type = MessageDTO.MessageType.IMAGE;
            } else {
                this.type = MessageDTO.MessageType.DOCUMENT;
            }
        }
    }

    public MessageDTO.MessageType getType() {
        return type != null ? type : MessageDTO.MessageType.TEXT;
    }
}