package dev.petr.bookswap.dto;

import jakarta.validation.constraints.*;
import java.util.Set;

public record BookCreateRequest(
        @NotBlank @Size(max=255) String title,
        @NotBlank @Size(max=255) String author,
        @Size(max=32) String isbn,
        @Min(0) @Max(2100) Integer publishedYear,
        @NotNull String condition,           // enum name
        Set<Long> genreIds
) {}
