package com.rescueme.repository;

import com.rescueme.repository.entity.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, Long> {

    // Găsește toate atașamentele pentru un mesaj
    List<MessageAttachment> findByMessageId(Long messageId);

    // Găsește toate atașamentele pentru o listă de mesaje
    @Query("SELECT a FROM MessageAttachment a WHERE a.messageId IN :messageIds")
    List<MessageAttachment> findByMessageIdIn(List<Long> messageIds);

    // Șterge toate atașamentele pentru un mesaj
    void deleteByMessageId(Long messageId);

    // Numără atașamentele unui mesaj
    long countByMessageId(Long messageId);
}