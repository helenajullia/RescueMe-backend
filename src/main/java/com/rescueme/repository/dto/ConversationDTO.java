package com.rescueme.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private String conversationId;
    private Long participantId;
    private String participantUsername;
    private String participantProfilePicture;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private boolean hasUnreadMessages;
    private Long unreadCount;
    private Long participantRole; // 1 pt ADOPTER, 2 pt SHELTER
}