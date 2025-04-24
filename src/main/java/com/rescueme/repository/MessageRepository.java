package com.rescueme.repository;

import com.rescueme.repository.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Find all messages in a conversation
    List<Message> findByConversationIdOrderByTimestampAsc(String conversationId);

    // Find all conversations for a user (distinct conversationIds)
    @Query("SELECT DISTINCT m.conversationId FROM Message m WHERE m.senderId = ?1 OR m.recipientId = ?1")
    List<String> findConversationIdsByUser(Long userId);

    // Get unread messages count for a user
    @Query("SELECT COUNT(m) FROM Message m WHERE m.recipientId = ?1 AND m.read = false")
    Long countUnreadMessagesByRecipient(Long recipientId);

    // Mark messages as read
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.read = true WHERE m.conversationId = ?1 AND m.recipientId = ?2 AND m.read = false")
    void markConversationAsRead(String conversationId, Long recipientId);

    // Find most recent message for each conversation a user is part of
    @Query("SELECT m FROM Message m WHERE m.id IN " +
            "(SELECT MAX(m2.id) FROM Message m2 WHERE m2.conversationId IN " +
            "(SELECT DISTINCT m3.conversationId FROM Message m3 WHERE m3.senderId = ?1 OR m3.recipientId = ?1) " +
            "GROUP BY m2.conversationId)")
    List<Message> findLatestMessagesForUser(Long userId);
}