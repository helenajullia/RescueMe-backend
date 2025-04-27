package com.rescueme.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.VarbinaryJdbcType;

@Entity
@Table(name = "message_attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long messageId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String contentType;

    @Lob
    @JdbcType(VarbinaryJdbcType.class)
    @Column(name = "file_data", columnDefinition = "BYTEA")
    private byte[] fileData;

    @Column(name = "file_size")
    private Long fileSize;

    // Identificator pentru miniatura (pentru imagini)
    @Column(name = "has_thumbnail")
    private boolean hasThumbnail = false;

    @Lob
    @JdbcType(VarbinaryJdbcType.class)
    @Column(name = "thumbnail_data", columnDefinition = "BYTEA")
    private byte[] thumbnailData;
}