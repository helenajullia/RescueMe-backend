package com.rescueme.controller;

import com.rescueme.repository.dto.ConversationDTO;
import com.rescueme.repository.dto.MessageDTO;
import com.rescueme.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;


    /**
     * REST endpoint to send a message
     */
    @PostMapping("/send")
    public ResponseEntity<MessageDTO> sendMessage(@RequestBody MessageDTO messageDTO) {
        log.info("REST message endpoint called with: {}", messageDTO);
        MessageDTO sent = messageService.sendMessage(messageDTO);
        return ResponseEntity.ok(sent);
    }

    /**
     * REST endpoint to send a message with attachments
     */
    @PostMapping(value = "/send-with-attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendMessageWithAttachments(
            @RequestPart("message") MessageDTO messageDTO,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        log.info("REST message with attachments endpoint called: {}", messageDTO);

        try {
            if (files == null || files.isEmpty()) {
                return ResponseEntity.ok(messageService.sendMessage(messageDTO));
            }

            MessageDTO sent = messageService.sendMessageWithAttachments(messageDTO, files);
            return ResponseEntity.ok(sent);
        } catch (IOException e) {
            log.error("Error while processing attachments", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error while processing attachments: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error while sending the message", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error while sending the message: " + e.getMessage()));
        }
    }


    /**
     * WebSocket endpoint to send a message
     */
    @MessageMapping("/chat.send")
    public void sendMessageWebSocket(@Payload MessageDTO messageDTO) {
        log.info("WebSocket message received: {}", messageDTO);
        messageService.sendMessage(messageDTO);
    }


    /**
     * Get all messages in a conversation
     */
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<MessageDTO>> getConversationMessages(
            @PathVariable String conversationId,
            @RequestParam Long userId) {

        List<MessageDTO> messages = messageService.getConversationMessages(conversationId, userId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Mark all messages in a conversation as read for a user
     */
    @PostMapping("/conversation/{conversationId}/read")
    public ResponseEntity<Void> markConversationAsRead(
            @PathVariable String conversationId,
            @RequestParam Long userId) {

        messageService.markConversationAsRead(conversationId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * WebSocket endpoint to mark messages as read
     */
    @MessageMapping("/chat.read")
    public void markAsReadWebSocket(@Payload Map<String, Object> payload) {
        String conversationId = (String) payload.get("conversationId");
        Long userId = Long.valueOf(payload.get("userId").toString());
        log.info("WebSocket mark as read received: {}, user: {}", conversationId, userId);

        messageService.markConversationAsRead(conversationId, userId);
    }

    /**
     * Get all conversations for a user
     */
    @GetMapping("/conversations/{userId}")
    public ResponseEntity<List<ConversationDTO>> getUserConversations(@PathVariable Long userId) {
        List<ConversationDTO> conversations = messageService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    /**
     * Get unread messages count for a user
     */
    @GetMapping("/unread/{userId}")
    public ResponseEntity<Long> getUnreadMessagesCount(@PathVariable Long userId) {
        Long count = messageService.getUnreadMessagesCount(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * Get a conversation ID for two users
     */
    @GetMapping("/conversation-id")
    public ResponseEntity<Map<String, String>> getConversationId(
            @RequestParam Long user1Id,
            @RequestParam Long user2Id) {

        String conversationId = messageService.generateConversationId(user1Id, user2Id);
        return ResponseEntity.ok(Map.of("conversationId", conversationId));
    }
}