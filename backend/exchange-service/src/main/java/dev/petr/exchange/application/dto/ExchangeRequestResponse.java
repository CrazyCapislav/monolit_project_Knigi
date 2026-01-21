package dev.petr.exchange.application.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.OffsetDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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

