package dev.petr.bookswap.dto;

import java.time.OffsetDateTime;

public record ExchangeRequestResponse(
        Long id,
        Long requesterId,
        Long ownerId,
        Long bookRequestedId,
        Long bookOfferedId,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
