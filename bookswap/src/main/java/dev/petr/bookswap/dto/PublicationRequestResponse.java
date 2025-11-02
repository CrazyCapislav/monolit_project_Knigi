package dev.petr.bookswap.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.OffsetDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PublicationRequestResponse(
        Long id,
        Long requesterId,
        Long publisherId,
        String title,
        String author,
        String message,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime decidedAt
) {}
