package com.rescueme.service;

import com.rescueme.repository.dto.ConversationDTO;
import com.rescueme.repository.dto.MessageDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MessageService {

    MessageDTO sendMessage(MessageDTO messageDTO);

    MessageDTO sendMessageWithAttachments(MessageDTO messageDTO, List<MultipartFile> files) throws IOException;

    List<MessageDTO> getConversationMessages(String conversationId, Long userId);

    void markConversationAsRead(String conversationId, Long userId);

    List<ConversationDTO> getUserConversations(Long userId);

    Long getUnreadMessagesCount(Long userId);

    String generateConversationId(Long user1Id, Long user2Id);

    byte[] getAttachmentContent(Long attachmentId);

    byte[] getAttachmentThumbnail(Long attachmentId);
}