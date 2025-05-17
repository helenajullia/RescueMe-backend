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
    private MessageType type = MessageType.TEXT;

    private List<AttachmentDTO> attachments = new ArrayList<>();

    private String senderUsername;
    private String senderProfilePicture;
    private String recipientUsername;
    private String recipientProfilePicture;

    public enum MessageType {
        TEXT,         // text simplu
        IMAGE,        // mesaj cu imagine
        DOCUMENT,     // mesaj cu document
        MIXED         // mesaj cu mai multe atasamente
    }
}