package com.rescueme.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private Long senderId;
    private Long recipientId;
    private String content;
    private LocalDateTime timestamp;
    private boolean read;
    private String conversationId;
    private MessageType type = MessageType.TEXT; // Valoare implicită TEXT

    // Atașamente
    private List<AttachmentDTO> attachments = new ArrayList<>();

    // Sender and recipient details
    private String senderUsername;
    private String senderProfilePicture;
    private String recipientUsername;
    private String recipientProfilePicture;

    // Tip de mesaj
    public enum MessageType {
        TEXT,         // Mesaj text simplu
        IMAGE,        // Mesaj cu imagine
        DOCUMENT,     // Mesaj cu document
        MIXED         // Mesaj cu mai multe atașamente
    }
}