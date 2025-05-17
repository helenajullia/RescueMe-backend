package com.rescueme.service.implementation;

import com.rescueme.repository.MessageAttachmentRepository;
import com.rescueme.repository.MessageRepository;
import com.rescueme.repository.UserRepository;
import com.rescueme.repository.dto.AttachmentDTO;
import com.rescueme.repository.dto.ConversationDTO;
import com.rescueme.repository.dto.MessageDTO;
import com.rescueme.repository.entity.Message;
import com.rescueme.repository.entity.MessageAttachment;
import com.rescueme.repository.entity.Role;
import com.rescueme.repository.entity.User;
import com.rescueme.service.AttachmentService;
import com.rescueme.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final MessageAttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final AttachmentService attachmentService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public MessageDTO sendMessage(MessageDTO messageDTO) {
        User sender = userRepository.findById(messageDTO.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User recipient = userRepository.findById(messageDTO.getRecipientId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        Message message = new Message();
        message.setSenderId(messageDTO.getSenderId());
        message.setRecipientId(messageDTO.getRecipientId());
        message.setContent(messageDTO.getContent());
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);
        message.setConversationId(generateConversationId(messageDTO.getSenderId(), messageDTO.getRecipientId()));
        message.setType(messageDTO.getType());

        Message savedMessage = messageRepository.save(message);

        MessageDTO responseDTO = convertToDTO(savedMessage, sender, recipient, new ArrayList<>());

        try {
            String recipientId = recipient.getId().toString();
            System.out.println("Sending WebSocket message to recipient ID: " + recipientId);

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
    @Transactional
    public MessageDTO sendMessageWithAttachments(MessageDTO messageDTO, List<MultipartFile> files) throws IOException {
        User sender = userRepository.findById(messageDTO.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User recipient = userRepository.findById(messageDTO.getRecipientId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        Message message = new Message();
        message.setSenderId(messageDTO.getSenderId());
        message.setRecipientId(messageDTO.getRecipientId());
        message.setContent(messageDTO.getContent());
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);
        message.setConversationId(generateConversationId(messageDTO.getSenderId(), messageDTO.getRecipientId()));
        message.setType(MessageDTO.MessageType.TEXT);

        Message savedMessage = messageRepository.save(message);

        List<MessageAttachment> attachments = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            attachments = attachmentService.createAttachments(files, savedMessage.getId());

            boolean hasImages = attachments.stream()
                    .anyMatch(a -> a.getContentType().startsWith("image/"));
            boolean hasDocs = attachments.stream()
                    .anyMatch(a -> !a.getContentType().startsWith("image/"));

            if (hasImages && hasDocs) {
                savedMessage.setType(MessageDTO.MessageType.MIXED);
            } else if (hasImages) {
                savedMessage.setType(MessageDTO.MessageType.IMAGE);
            } else if (hasDocs) {
                savedMessage.setType(MessageDTO.MessageType.DOCUMENT);
            }

            savedMessage = messageRepository.save(savedMessage);
        }

        List<AttachmentDTO> attachmentDTOs = attachments.stream()
                .map(a -> attachmentService.convertToDTO(a, false))
                .collect(Collectors.toList());

        MessageDTO responseDTO = convertToDTO(savedMessage, sender, recipient, attachmentDTOs);

        try {
            String recipientId = recipient.getId().toString();
            System.out.println("Sending WebSocket message with attachments to recipient ID: " + recipientId);

            messagingTemplate.convertAndSend(
                    "/topic/chat/" + recipientId,
                    responseDTO
            );

            System.out.println("WebSocket message with attachments sent to /topic/chat/" + recipientId);
            log.info("Message with attachments sent via WebSocket to user: {}", recipient.getId());
        } catch (Exception e) {
            log.error("Failed to send message with attachments via WebSocket", e);
            e.printStackTrace();
        }

        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageDTO> getConversationMessages(String conversationId, Long userId) {
        if (!isUserInConversation(userId, conversationId)) {
            throw new RuntimeException("User is not part of this conversation");
        }

        List<Message> messages = messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);

        Set<Long> userIds = messages.stream()
                .flatMap(m -> Stream.of(m.getSenderId(), m.getRecipientId()))
                .collect(Collectors.toSet());

        Map<Long, User> usersMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        List<Long> messageIds = messages.stream()
                .map(Message::getId)
                .collect(Collectors.toList());

        List<MessageAttachment> allAttachments = attachmentRepository.findByMessageIdIn(messageIds);

        Map<Long, List<MessageAttachment>> attachmentsByMessageId = allAttachments.stream()
                .collect(Collectors.groupingBy(MessageAttachment::getMessageId));

        return messages.stream()
                .map(message -> {
                    User sender = usersMap.get(message.getSenderId());
                    User recipient = usersMap.get(message.getRecipientId());

                    List<MessageAttachment> messageAttachments = attachmentsByMessageId.getOrDefault(message.getId(), new ArrayList<>());
                    List<AttachmentDTO> attachmentDTOs = messageAttachments.stream()
                            .map(a -> attachmentService.convertToDTO(a, false))
                            .collect(Collectors.toList());

                    return convertToDTO(message, sender, recipient, attachmentDTOs);
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

        String[] userIds = conversationId.split("_");
        Long partnerId = userIds[0].equals(userId.toString()) ?
                Long.parseLong(userIds[1]) : Long.parseLong(userIds[0]);

        messagingTemplate.convertAndSend(
                "/topic/read/" + partnerId,
                Map.of("conversationId", conversationId, "readBy", userId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDTO> getUserConversations(Long userId) {
        List<String> conversationIds = messageRepository.findConversationIdsByUser(userId);

        List<ConversationDTO> conversations = new ArrayList<>();

        for (String conversationId : conversationIds) {
            Long participantId = parseParticipantId(conversationId, userId);

            User participant = userRepository.findById(participantId)
                    .orElse(null);

            if (participant == null) continue;

            List<Message> conversationMessages = messageRepository.findByConversationIdOrderByTimestampAsc(conversationId);
            if (conversationMessages.isEmpty()) continue;

            Message latestMessage = conversationMessages.get(conversationMessages.size() - 1);

            long unreadCount = conversationMessages.stream()
                    .filter(m -> m.getRecipientId().equals(userId) && !m.isRead())
                    .count();

            ConversationDTO conversationDTO = new ConversationDTO();
            conversationDTO.setConversationId(conversationId);
            conversationDTO.setParticipantId(participantId);
            conversationDTO.setParticipantUsername(participant.getUsername());
            conversationDTO.setParticipantProfilePicture(
                    participant.getProfilePicture() != null ?
                            Base64.getEncoder().encodeToString(participant.getProfilePicture()) : null
            );

            String lastMessageContent = latestMessage.getContent();
            if (latestMessage.getType() != MessageDTO.MessageType.TEXT) {
                long attachmentCount = attachmentRepository.countByMessageId(latestMessage.getId());

                switch (latestMessage.getType()) {
                    case IMAGE:
                        lastMessageContent = attachmentCount > 1
                                ? attachmentCount + " images"
                                : "📷 Image";
                        break;
                    case DOCUMENT:
                        lastMessageContent = attachmentCount > 1
                                ? attachmentCount + " documents"
                                : "📄 Document";
                        break;
                    case MIXED:
                        lastMessageContent = "📎 " + attachmentCount + " attachments";
                        break;
                }

                if (!latestMessage.getContent().trim().isEmpty()) {
                    lastMessageContent = latestMessage.getContent() + " [" + lastMessageContent + "]";
                }
            }

            conversationDTO.setLastMessage(lastMessageContent);
            conversationDTO.setLastMessageTime(latestMessage.getTimestamp());
            conversationDTO.setHasUnreadMessages(unreadCount > 0);
            conversationDTO.setUnreadCount(unreadCount);

            conversationDTO.setParticipantRole(
                    participant.getRole() == Role.ADOPTER ? 1L : 2L
            );

            conversations.add(conversationDTO);
        }

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
        long smallerId = Math.min(user1Id, user2Id);
        long largerId = Math.max(user1Id, user2Id);
        return smallerId + "_" + largerId;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getAttachmentContent(Long attachmentId) {
        MessageAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Attachment not found with ID: " + attachmentId));

        return attachment.getFileData();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getAttachmentThumbnail(Long attachmentId) {
        MessageAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Attachment not found with ID: " + attachmentId));

        if (!attachment.isHasThumbnail() || attachment.getThumbnailData() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Thumbnail does not exist for attachment with ID: " + attachmentId);
        }

        return attachment.getThumbnailData();
    }


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

    private MessageDTO convertToDTO(Message message, User sender, User recipient, List<AttachmentDTO> attachments) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setSenderId(message.getSenderId());
        dto.setRecipientId(message.getRecipientId());
        dto.setContent(message.getContent());
        dto.setTimestamp(message.getTimestamp());
        dto.setRead(message.isRead());
        dto.setConversationId(message.getConversationId());

        dto.setType(message.getType() != null ? message.getType() : MessageDTO.MessageType.TEXT);

        dto.setAttachments(attachments);

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