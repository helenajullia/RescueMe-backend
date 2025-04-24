package com.rescueme.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    // Sender and recipient details
    private String senderUsername;
    private String senderProfilePicture;
    private String recipientUsername;
    private String recipientProfilePicture;
}