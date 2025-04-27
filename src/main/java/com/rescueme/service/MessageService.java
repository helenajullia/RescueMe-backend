package com.rescueme.service;

import com.rescueme.repository.dto.ConversationDTO;
import com.rescueme.repository.dto.MessageDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MessageService {

    /**
     * Trimite un mesaj text
     */
    MessageDTO sendMessage(MessageDTO messageDTO);

    /**
     * Trimite un mesaj cu atașamente
     */
    MessageDTO sendMessageWithAttachments(MessageDTO messageDTO, List<MultipartFile> files) throws IOException;

    /**
     * Obține toate mesajele dintr-o conversație
     */
    List<MessageDTO> getConversationMessages(String conversationId, Long userId);

    /**
     * Marchează mesajele dintr-o conversație ca fiind citite
     */
    void markConversationAsRead(String conversationId, Long userId);

    /**
     * Obține toate conversațiile unui utilizator
     */
    List<ConversationDTO> getUserConversations(Long userId);

    /**
     * Numărul de mesaje necitite pentru un utilizator
     */
    Long getUnreadMessagesCount(Long userId);

    /**
     * Generează un ID de conversație pentru doi utilizatori
     */
    String generateConversationId(Long user1Id, Long user2Id);

    /**
     * Obține un atașament după ID
     */
    byte[] getAttachmentContent(Long attachmentId);

    /**
     * Obține miniatura unui atașament
     */
    byte[] getAttachmentThumbnail(Long attachmentId);
}