package dev.petr.bookswap.dto;

import jakarta.validation.constraints.NotBlank;

public record PublicationRequestCreateRequest(
        @NotBlank String title,
        @NotBlank String author,
        String message,
        Long publisherId               // конкретное издательство
) {}
