package dev.petr.book.application.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record BookUpdateRequest(
        @NotBlank @Size(max = 500) String title,
        @NotBlank @Size(max = 300) String author,
        @Size(max = 20) String isbn,
        @Min(1000) @Max(9999) Integer publishedYear,
        @NotBlank String condition,
        @NotBlank String status,
        Set<Long> genreIds
) {}

