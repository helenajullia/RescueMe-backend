package com.rescueme.service.implementation;

import com.rescueme.repository.MessageAttachmentRepository;
import com.rescueme.repository.dto.AttachmentDTO;
import com.rescueme.repository.entity.MessageAttachment;
import com.rescueme.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final MessageAttachmentRepository attachmentRepository;

    // Dimensiunea maximă a fișierului (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // Dimensiunea maximă a miniaturilor
    private static final int THUMBNAIL_WIDTH = 200;
    private static final int THUMBNAIL_HEIGHT = 200;

    @Override
    @Transactional
    public MessageAttachment createAttachment(MultipartFile file, Long messageId) throws IOException {
        // Verifică dimensiunea fișierului
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Fișierul depășește dimensiunea maximă permisă (5MB)");
        }

        MessageAttachment attachment = new MessageAttachment();
        attachment.setMessageId(messageId);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setContentType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setFileData(file.getBytes());

        // Generează miniatură pentru imagini
        if (file.getContentType() != null && file.getContentType().startsWith("image/")) {
            try {
                byte[] thumbnail = generateThumbnail(file.getBytes());
                attachment.setThumbnailData(thumbnail);
                attachment.setHasThumbnail(true);
            } catch (Exception e) {
                log.warn("Nu s-a putut genera miniatura pentru imaginea: {}", file.getOriginalFilename(), e);
                attachment.setHasThumbnail(false);
            }
        }

        return attachmentRepository.save(attachment);
    }

    @Override
    @Transactional
    public List<MessageAttachment> createAttachments(List<MultipartFile> files, Long messageId) throws IOException {
        List<MessageAttachment> attachments = new ArrayList<>();

        for (MultipartFile file : files) {
            attachments.add(createAttachment(file, messageId));
        }

        return attachments;
    }

    @Override
    @Transactional(readOnly = true)
    public MessageAttachment getAttachment(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Atașament negăsit cu ID: " + attachmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageAttachment> getAttachmentsForMessage(Long messageId) {
        return attachmentRepository.findByMessageId(messageId);
    }

    @Override
    @Transactional(readOnly = true)
    public AttachmentDTO getAttachmentDTO(Long attachmentId) {
        MessageAttachment attachment = getAttachment(attachmentId);
        return convertToDTO(attachment, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentDTO> getAttachmentInfoForMessage(Long messageId) {
        List<MessageAttachment> attachments = attachmentRepository.findByMessageId(messageId);
        return attachments.stream()
                .map(a -> convertToDTO(a, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId) {
        if (!attachmentRepository.existsById(attachmentId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Atașament negăsit cu ID: " + attachmentId);
        }

        attachmentRepository.deleteById(attachmentId);
    }

    @Override
    public byte[] generateThumbnail(byte[] imageData) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
        BufferedImage originalImage = ImageIO.read(bis);

        // Calculează dimensiunile pentru a păstra raportul de aspect
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        double aspectRatio = (double) originalWidth / originalHeight;

        int width = THUMBNAIL_WIDTH;
        int height = (int) (width / aspectRatio);

        if (height > THUMBNAIL_HEIGHT) {
            height = THUMBNAIL_HEIGHT;
            width = (int) (height * aspectRatio);
        }

        // Crează imaginea redimensionată
        BufferedImage thumbnailImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = thumbnailImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();

        // Convertește imaginea înapoi în bytes
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(thumbnailImage, "jpg", bos);

        return bos.toByteArray();
    }

    @Override
    public AttachmentDTO convertToDTO(MessageAttachment attachment, boolean includeContent) {
        AttachmentDTO dto = new AttachmentDTO();
        dto.setId(attachment.getId());
        dto.setFileName(attachment.getFileName());
        dto.setContentType(attachment.getContentType());
        dto.setFileSize(attachment.getFileSize());
        dto.setHasThumbnail(attachment.isHasThumbnail());

        if (includeContent) {
            if (attachment.getFileData() != null) {
                dto.setFileData(Base64.getEncoder().encodeToString(attachment.getFileData()));
            }

            if (attachment.isHasThumbnail() && attachment.getThumbnailData() != null) {
                dto.setThumbnailData(Base64.getEncoder().encodeToString(attachment.getThumbnailData()));
            }
        }

        return dto;
    }
}