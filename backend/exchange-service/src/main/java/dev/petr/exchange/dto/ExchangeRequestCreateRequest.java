package dev.petr.exchange.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ExchangeRequestCreateRequest(
        @NotNull Long bookRequestedId,
        Long bookOfferedId
) {}
