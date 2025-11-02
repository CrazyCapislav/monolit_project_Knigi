package dev.petr.bookswap.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.*;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UserCreateRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min=3,max=120) String displayName,
        @NotBlank String password
) {}
