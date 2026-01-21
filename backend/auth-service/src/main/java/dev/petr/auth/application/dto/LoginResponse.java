package dev.petr.auth.application.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record LoginResponse(
        String token,
        String tokenType,
        Long expiresIn,
        UserResponse user
) {
    public LoginResponse(String token, Long expiresIn, UserResponse user) {
        this(token, "Bearer", expiresIn, user);
    }
}

