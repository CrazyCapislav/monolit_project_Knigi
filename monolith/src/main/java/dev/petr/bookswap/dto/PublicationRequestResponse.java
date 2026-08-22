package dev.petr.bookswap.dto;

import java.time.OffsetDateTime;

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
