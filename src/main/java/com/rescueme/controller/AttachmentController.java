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
     * Returns attachment info without including the file data or thumbnail
     */
    @GetMapping("/{attachmentId}/info")
    public ResponseEntity<AttachmentDTO> getAttachmentInfo(@PathVariable Long attachmentId) {
        try {
            AttachmentDTO attachmentDTO = attachmentService.getAttachmentDTO(attachmentId);
            attachmentDTO.setFileData(null);
            attachmentDTO.setThumbnailData(null);
            return ResponseEntity.ok(attachmentDTO);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            log.error("Error retrieving attachment information", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Returns the binary content of an attachment for downloading
     */
    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Long attachmentId) {
        try {
            byte[] fileData = messageService.getAttachmentContent(attachmentId);
            AttachmentDTO info = attachmentService.getAttachmentDTO(attachmentId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(info.getContentType()));
            headers.setContentDispositionFormData("attachment", info.getFileName());

            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(fileData);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            log.error("Error downloading the attachment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Returns the thumbnail image of an attachment as JPEG
     */
    @GetMapping("/{attachmentId}/thumbnail")
    public ResponseEntity<byte[]> getAttachmentThumbnail(@PathVariable Long attachmentId) {
        try {
            byte[] thumbnailData = messageService.getAttachmentThumbnail(attachmentId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(thumbnailData);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            log.error("Error retrieving attachment thumbnail", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deletes an attachment with the specified ID
     */
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long attachmentId) {
        try {
            attachmentService.deleteAttachment(attachmentId);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            log.error("Error deleting the attachment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}