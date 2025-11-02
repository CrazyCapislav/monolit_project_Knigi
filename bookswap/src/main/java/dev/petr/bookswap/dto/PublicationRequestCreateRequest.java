package dev.petr.bookswap.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PublicationRequestCreateRequest(
        @NotBlank String title,
        @NotBlank String author,
        String message,
        Long publisherId
) {}
