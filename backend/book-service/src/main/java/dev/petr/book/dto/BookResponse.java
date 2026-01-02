package dev.petr.book.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.OffsetDateTime;
import java.util.Set;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
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
