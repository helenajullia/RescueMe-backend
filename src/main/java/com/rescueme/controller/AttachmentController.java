package com.rescueme.controller;

import com.rescueme.repository.dto.AttachmentDTO;
import com.rescueme.service.AttachmentService;
import com.rescueme.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
@Slf4j
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final MessageService messageService;

    /**
     * Obține informații despre un atașament
     */
    @GetMapping("/{attachmentId}/info")
    public ResponseEntity<AttachmentDTO> getAttachmentInfo(@PathVariable Long attachmentId) {
        try {
            AttachmentDTO attachmentDTO = attachmentService.getAttachmentDTO(attachmentId);
            // Excludem conținutul binar
            attachmentDTO.setFileData(null);
            attachmentDTO.setThumbnailData(null);
            return ResponseEntity.ok(attachmentDTO);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            log.error("Eroare la obținerea informațiilor atașamentului", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Descarcă conținutul unui atașament
     */
    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long attachmentId) {
        try {
            byte[] fileData = messageService.getAttachmentContent(attachmentId);
            AttachmentDTO info = attachmentService.getAttachmentDTO(attachmentId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(info.getContentType()));
            headers.setContentDispositionFormData("attachment", info.getFileName());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileData);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            log.error("Eroare la descărcarea atașamentului", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obține miniatura unui atașament (pentru imagini)
     */
    @GetMapping("/{attachmentId}/thumbnail")
    public ResponseEntity<byte[]> getAttachmentThumbnail(@PathVariable Long attachmentId) {
        try {
            byte[] thumbnailData = messageService.getAttachmentThumbnail(attachmentId);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(thumbnailData);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            log.error("Eroare la obținerea miniaturii atașamentului", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Șterge un atașament
     */
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long attachmentId) {
        try {
            attachmentService.deleteAttachment(attachmentId);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            log.error("Eroare la ștergerea atașamentului", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}