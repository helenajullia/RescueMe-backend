package com.rescueme.service.implementation;

import com.rescueme.repository.MessageRepository;
import com.rescueme.repository.UserRepository;
import com.rescueme.repository.dto.ConversationDTO;
import com.rescueme.repository.dto.MessageDTO;
import com.rescueme.repository.entity.Message;
import com.rescueme.repository.entity.Role;
import com.rescueme.repository.entity.User;
import com.rescueme.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public MessageDTO sendMessage(MessageDTO messageDTO) {
        // Validate sender and recipient
        User sender = userRepository.findById(messageDTO.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User recipient = userRepository.findById(messageDTO.getRecipientId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        // Create and save the message
        Message message = new Message();
        message.setSenderId(messageDTO.getSenderId());
        message.setRecipientId(messageDTO.getRecipientId());
        message.setContent(messageDTO.getContent());
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);
        message.setConversationId(generateConversationId(messageDTO.getSenderId(), messageDTO.getRecipientId()));

        Message savedMessage = messageRepository.save(message);

        // Create response DTO with additional user information
        MessageDTO responseDTO = convertToDTO(savedMessage, sender, recipient);

        // MODIFICAT: Trimitem mesajul direct la topic în loc de user queue
        try {
            String recipientId = recipient.getId().toString();
            System.out.println("Sending WebSocket message to recipient ID: " + recipientId);

            // Folosim topic în loc de user queue
            messagingTemplate.convertAndSend(
                    "/topic/chat/" + recipientId,
                    responseDTO
            );

            System.out.println("WebSocket message sent to /topic/chat/" + recipientId);
            log.info("Message sent via WebSocket to user: {}", recipient.getId());
        } catch (Exception e) {
            log.error("Failed to send message via WebSocket", e);
            e.printStackTrace();
        }

        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageDTO> getConversationMessages(String conversationId, Long userId) {
        // Check user is part of the conversation
        if (!isUserInConversation(userId, conversationId)) {
            throw new RuntimeException("User is not part of this conversation");
        }

        List<Message> messages = messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);

        // Get all unique user IDs in the conversation
        Set<Long> userIds = messages.stream()
                .flatMap(m -> Stream.of(m.getSenderId(), m.getRecipientId()))
                .collect(Collectors.toSet());

        // Get all users in the conversation
        Map<Long, User> usersMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // Convert to DTOs with user information
        return messages.stream()
                .map(message -> {
                    User sender = usersMap.get(message.getSenderId());
                    User recipient = usersMap.get(message.getRecipientId());
                    return convertToDTO(message, sender, recipient);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markConversationAsRead(String conversationId, Long userId) {
        if (!isUserInConversation(userId, conversationId)) {
            throw new RuntimeException("User is not part of this conversation");
        }

        messageRepository.markConversationAsRead(conversationId, userId);

        // Notify the conversation partner about read status change
        String[] userIds = conversationId.split("_");
        Long partnerId = userIds[0].equals(userId.toString()) ?
                Long.parseLong(userIds[1]) : Long.parseLong(userIds[0]);

        // MODIFICAT: Trimitem notificarea de read la topic în loc de user queue
        messagingTemplate.convertAndSend(
                "/topic/read/" + partnerId,
                Map.of("conversationId", conversationId, "readBy", userId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDTO> getUserConversations(Long userId) {
        // Get all conversation IDs for this user
        List<String> conversationIds = messageRepository.findConversationIdsByUser(userId);

        // For each conversation ID, find the latest message
        List<ConversationDTO> conversations = new ArrayList<>();

        for (String conversationId : conversationIds) {
            // Parse the conversation ID to get the other participant's ID
            Long participantId = parseParticipantId(conversationId, userId);

            // Get the user information for this participant
            User participant = userRepository.findById(participantId)
                    .orElse(null);

            if (participant == null) continue;

            // Get the latest message for this conversation
            List<Message> conversationMessages = messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
            if (conversationMessages.isEmpty()) continue;

            Message latestMessage = conversationMessages.get(conversationMessages.size() - 1);

            // Count unread messages
            long unreadCount = conversationMessages.stream()
                    .filter(m -> m.getRecipientId().equals(userId) && !m.isRead())
                    .count();

            // Create a DTO
            ConversationDTO conversationDTO = new ConversationDTO();
            conversationDTO.setConversationId(conversationId);
            conversationDTO.setParticipantId(participantId);
            conversationDTO.setParticipantUsername(participant.getUsername());
            conversationDTO.setParticipantProfilePicture(
                    participant.getProfilePicture() != null ?
                            Base64.getEncoder().encodeToString(participant.getProfilePicture()) : null
            );
            conversationDTO.setLastMessage(latestMessage.getContent());
            conversationDTO.setLastMessageTime(latestMessage.getTimestamp());
            conversationDTO.setHasUnreadMessages(unreadCount > 0);
            conversationDTO.setUnreadCount(unreadCount);

            // Set participant role (1 for ADOPTER, 2 for SHELTER)
            conversationDTO.setParticipantRole(
                    participant.getRole() == Role.ADOPTER ? 1L : 2L
            );

            conversations.add(conversationDTO);
        }

        // Sort conversations by the timestamp of the latest message (newest first)
        conversations.sort((c1, c2) -> c2.getLastMessageTime().compareTo(c1.getLastMessageTime()));

        return conversations;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadMessagesCount(Long userId) {
        return messageRepository.countUnreadMessagesByRecipient(userId);
    }

    @Override
    public String generateConversationId(Long user1Id, Long user2Id) {
        // Create a consistent conversationId by sorting the user IDs
        long smallerId = Math.min(user1Id, user2Id);
        long largerId = Math.max(user1Id, user2Id);
        return smallerId + "_" + largerId;
    }

    // Helper methods
    private boolean isUserInConversation(Long userId, String conversationId) {
        String[] parts = conversationId.split("_");
        if (parts.length != 2) return false;

        try {
            Long user1 = Long.parseLong(parts[0]);
            Long user2 = Long.parseLong(parts[1]);
            return userId.equals(user1) || userId.equals(user2);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private Long parseParticipantId(String conversationId, Long userId) {
        String[] parts = conversationId.split("_");
        if (parts.length != 2) {
            throw new RuntimeException("Invalid conversation ID format");
        }

        try {
            Long user1 = Long.parseLong(parts[0]);
            Long user2 = Long.parseLong(parts[1]);

            return userId.equals(user1) ? user2 : user1;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid conversation ID format");
        }
    }

    private MessageDTO convertToDTO(Message message, User sender, User recipient) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSenderId(message.getSenderId());
        dto.setRecipientId(message.getRecipientId());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        dto.setRead(message.isRead());
        dto.setConversationId(message.getConversationId());

        // Add user information
        if (sender != null) {
            dto.setSenderUsername(sender.getUsername());
            if (sender.getProfilePicture() != null) {
                dto.setSenderProfilePicture(Base64.getEncoder().encodeToString(sender.getProfilePicture()));
            }
        }

        if (recipient != null) {
            dto.setRecipientUsername(recipient.getUsername());
            if (recipient.getProfilePicture() != null) {
                dto.setRecipientProfilePicture(Base64.getEncoder().encodeToString(recipient.getProfilePicture()));
            }
        }

        return dto;
    }
}