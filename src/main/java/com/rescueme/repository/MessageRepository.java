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
    List<Message> findByConversationIdOrderByTimestampAsc(String conversationId);

    @Query("SELECT DISTINCT m.conversationId FROM Message m WHERE m.senderId = ?1 OR m.recipientId = ?1")
    List<String> findConversationIdsByUser(Long userId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.recipientId = ?1 AND m.read = false")
    Long countUnreadMessagesByRecipient(Long recipientId);

    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.read = true WHERE m.conversationId = ?1 AND m.recipientId = ?2 AND m.read = false")
    void markConversationAsRead(String conversationId, Long recipientId);
}