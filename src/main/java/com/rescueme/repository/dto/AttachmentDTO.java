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
    private String fileData;
    private Long fileSize;
    private boolean hasThumbnail;
    private String thumbnailData;
}