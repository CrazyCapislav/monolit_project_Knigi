package dev.petr.file.application.dto;

import java.time.OffsetDateTime;

public record FileMetadataResponse(
        Long id,
        String fileName,
        String originalName,
        String fileType,
        Long fileSize,
        Long uploaderId,
        String entityType,
        Long entityId,
        String downloadUrl,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}