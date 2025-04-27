package com.rescueme.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDTO {
    private Long id;
    private String fileName;
    private String contentType;
    private String fileData; // Base64 encoded
    private Long fileSize;
    private boolean hasThumbnail;
    private String thumbnailData; // Base64 encoded

    // Constructor fără date pentru transferul în rețea
    public AttachmentDTO(Long id, String fileName, String contentType, Long fileSize, boolean hasThumbnail) {
        this.id = id;
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.hasThumbnail = hasThumbnail;
        this.fileData = null; // Nu includem datele în răspunsuri pentru a economisi lățime de bandă
        this.thumbnailData = null;
    }

    // Determină dacă atașamentul este o imagine
    public boolean isImage() {
        return contentType != null && contentType.startsWith("image/");
    }

    // Determină dacă atașamentul este un document
    public boolean isDocument() {
        return !isImage();
    }
}