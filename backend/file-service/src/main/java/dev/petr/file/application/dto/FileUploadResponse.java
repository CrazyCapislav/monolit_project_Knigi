package dev.petr.file.application.dto;

import java.time.OffsetDateTime;

public record FileUploadResponse(
        Long id,
        String fileName,
        String originalName,
        String fileType,
        Long fileSize,
        String downloadUrl,
        OffsetDateTime uploadedAt
) {}