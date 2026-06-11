package com.rescueme.service;

import com.rescueme.repository.dto.AttachmentDTO;
import com.rescueme.repository.entity.MessageAttachment;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AttachmentService {

    MessageAttachment createAttachment(MultipartFile file, Long messageId) throws IOException;

    List<MessageAttachment> createAttachments(List<MultipartFile> files, Long messageId) throws IOException;

    MessageAttachment getAttachment(Long attachmentId);

    List<MessageAttachment> getAttachmentsForMessage(Long messageId);

    AttachmentDTO getAttachmentDTO(Long attachmentId);

    List<AttachmentDTO> getAttachmentInfoForMessage(Long messageId);

    void deleteAttachment(Long attachmentId);

    byte[] generateThumbnail(byte[] imageData) throws IOException;

    AttachmentDTO convertToDTO(MessageAttachment attachment, boolean includeContent);
}