package com.rescueme.repository;

import com.rescueme.repository.entity.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, Long> {
    List<MessageAttachment> findByMessageId(Long messageId);

    @Query("SELECT a FROM MessageAttachment a WHERE a.messageId IN :messageIds")
    List<MessageAttachment> findByMessageIdIn(List<Long> messageIds);

    long countByMessageId(Long messageId);
}