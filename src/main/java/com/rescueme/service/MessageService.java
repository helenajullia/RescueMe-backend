package com.rescueme.service;

import com.rescueme.repository.dto.ConversationDTO;
import com.rescueme.repository.dto.MessageDTO;
import com.rescueme.repository.entity.Message;
import java.util.List;

public interface MessageService {

    // Send a new message
    MessageDTO sendMessage(MessageDTO messageDTO);

    // Get all messages in a conversation
    List<MessageDTO> getConversationMessages(String conversationId, Long userId);

    // Mark all messages in a conversation as read for a user
    void markConversationAsRead(String conversationId, Long userId);

    // Get all conversations for a user
    List<ConversationDTO> getUserConversations(Long userId);

    // Get unread messages count for a user
    Long getUnreadMessagesCount(Long userId);

    // Generate conversation ID from two user IDs
    String generateConversationId(Long user1Id, Long user2Id);
}