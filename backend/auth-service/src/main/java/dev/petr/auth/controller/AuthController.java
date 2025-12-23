package dev.petr.auth.controller;

import dev.petr.auth.dto.*;
import dev.petr.auth.service.AuthService;
import dev.petr.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    /**
     * Public registration (role = USER by default)
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for email: {}", request.email());
        return userService.register(request);
    }

    /**
     * Login
     */
    @PostMapping("/login")
    public Mono<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for email: {}", request.email());
        return authService.login(request);
    }

    /**
     * Get current user info by user ID
     */
    @GetMapping("/me")
    public Mono<UserResponse> getCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader
    ) {
        log.info("Get current user request");

        if (userIdHeader == null) {
            log.warn("Missing X-User-Id header");
            return Mono.error(new IllegalArgumentException("X-User-Id header required"));
        }

        try {
            Long userId = Long.parseLong(userIdHeader);
            log.info("Getting user with ID: {}", userId);
            return userService.findById(userId);
        } catch (NumberFormatException e) {
            log.error("Invalid userId in header", e);
            return Mono.error(new IllegalArgumentException("Invalid user ID format"));
        }
    }

    /**
     * Create user with specific role (admin only)
     */
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String roleHeader
    ) {
        log.info("Create user request for email: {}", request.email());

        if (roleHeader == null || !"ADMIN".equals(roleHeader)) {
            log.warn("Non-admin user attempted to create user. Role: {}", roleHeader);
            return Mono.error(new IllegalArgumentException("Only admins can create users"));
        }

        log.info("Admin creating user with email: {} and role: {}", request.email(), request.role());
        return userService.createUser(request);
    }
}