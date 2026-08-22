package dev.petr.bookswap.dto;

import jakarta.validation.constraints.NotNull;

public record ExchangeRequestCreateRequest(
        @NotNull Long bookRequestedId,
        Long bookOfferedId
) {}
