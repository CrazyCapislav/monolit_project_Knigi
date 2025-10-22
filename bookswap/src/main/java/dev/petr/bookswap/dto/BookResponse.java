package dev.petr.bookswap.dto;

import java.time.OffsetDateTime;
import java.util.Set;

public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        Integer publishedYear,
        String status,
        String condition,
        OffsetDateTime createdAt,
        Long ownerId,
        Set<String> genres
) {}
