package dev.petr.bookswap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenreCreateRequest(
        @NotBlank
        @Size(min = 2, max = 60)
        String name) {}
