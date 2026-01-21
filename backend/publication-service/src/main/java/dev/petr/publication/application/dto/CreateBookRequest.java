package dev.petr.publication.application.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CreateBookRequest(
        @NotBlank String title,
        @NotBlank String author,
        String isbn,
        Integer publishedYear,
        @NotNull String condition,
        String description,
        @NotNull Set<Long> genreIds
) {}
