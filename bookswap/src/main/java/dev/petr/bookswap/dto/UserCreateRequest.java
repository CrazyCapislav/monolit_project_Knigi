package dev.petr.bookswap.dto;

import jakarta.validation.constraints.*;

public record UserCreateRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(min=3,max=120) String displayName,
        @NotBlank String password         // plain, демо; для ЛР-2 будет хэш
) {}
