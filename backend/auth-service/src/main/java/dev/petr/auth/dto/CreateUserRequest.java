package dev.petr.auth.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CreateUserRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 3, max = 120) String displayName,
        @NotBlank @Size(min = 6) String password,
        @NotBlank String role
) {}
