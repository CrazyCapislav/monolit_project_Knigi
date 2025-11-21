package dev.petr.publication.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PublicationRequestCreateRequest(
        @NotBlank @Size(max = 500) String title,
        @NotBlank @Size(max = 200) String author,
        @Size(max = 20) String isbn,
        Integer publishedYear,
        String description
) {}