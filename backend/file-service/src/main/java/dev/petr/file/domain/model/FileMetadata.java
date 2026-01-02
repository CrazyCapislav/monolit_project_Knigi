package dev.petr.file.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class FileMetadata {
    private Long id;
    private String fileName;
    private String originalName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private Long uploaderId;
    private String entityType;
    private Long entityId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}