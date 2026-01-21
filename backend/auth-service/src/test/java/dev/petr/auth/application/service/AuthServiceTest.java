package dev.petr.auth.application.service;

import dev.petr.auth.application.dto.LoginRequest;
import dev.petr.auth.application.dto.LoginResponse;
import dev.petr.auth.domain.model.Role;
import dev.petr.auth.domain.model.User;
import dev.petr.auth.infrastructure.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .displayName("Test User")
                .passwordHash("$2a$10$hashedPassword")
                .role(Role.USER)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void login_ValidCredentials_ReturnsToken() {
        LoginRequest request = new LoginRequest("test@test.com", "password123");

        when(userService.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(userService.verifyPassword(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtUtil.getExpirationMillis()).thenReturn(3600000L);

        Mono<LoginResponse> result = authService.login(request);

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.token().equals("jwt-token") &&
                                response.user().email().equals("test@test.com")
                )
                .verifyComplete();
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        LoginRequest request = new LoginRequest("test@test.com", "wrongPassword");

        when(userService.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(userService.verifyPassword(anyString(), anyString())).thenReturn(false);

        Mono<LoginResponse> result = authService.login(request);

        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        LoginRequest request = new LoginRequest("nonexistent@test.com", "password");

        when(userService.findByEmail(anyString()))
                .thenReturn(Mono.error(new IllegalArgumentException("User not found")));

        Mono<LoginResponse> result = authService.login(request);

        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}
