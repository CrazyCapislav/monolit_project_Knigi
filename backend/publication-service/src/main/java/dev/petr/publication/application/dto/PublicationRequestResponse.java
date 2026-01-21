package dev.petr.publication.application.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.OffsetDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PublicationRequestResponse(
        Long id,
        Long requesterId,
        String title,
        String author,
        String isbn,
        Integer publishedYear,
        String description,
        String status,
        Long publisherId,
        Long createdBookId,
        String rejectionReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
