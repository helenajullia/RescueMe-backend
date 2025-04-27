package com.rescueme.service;

import com.rescueme.repository.dto.AttachmentDTO;
import com.rescueme.repository.entity.MessageAttachment;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AttachmentService {

    /**
     * Creează un atașament din fișierul încărcat
     */
    MessageAttachment createAttachment(MultipartFile file, Long messageId) throws IOException;

    /**
     * Creează mai multe atașamente din fișierele încărcate
     */
    List<MessageAttachment> createAttachments(List<MultipartFile> files, Long messageId) throws IOException;

    /**
     * Obține un atașament după ID
     */
    MessageAttachment getAttachment(Long attachmentId);

    /**
     * Obține toate atașamentele pentru un mesaj
     */
    List<MessageAttachment> getAttachmentsForMessage(Long messageId);

    /**
     * Obține DTO-ul unui atașament, inclusiv conținutul
     */
    AttachmentDTO getAttachmentDTO(Long attachmentId);

    /**
     * Obține DTO-urile tuturor atașamentelor pentru un mesaj, fără conținut
     */
    List<AttachmentDTO> getAttachmentInfoForMessage(Long messageId);

    /**
     * Șterge un atașament
     */
    void deleteAttachment(Long attachmentId);

    /**
     * Generează o miniatură pentru o imagine
     */
    byte[] generateThumbnail(byte[] imageData) throws IOException;

    /**
     * Convertește un atașament în DTO, opțional include conținutul fișierului
     */
    AttachmentDTO convertToDTO(MessageAttachment attachment, boolean includeContent);
}