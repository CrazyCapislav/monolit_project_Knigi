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
     * Login and get JWT token
     */
    @PostMapping("/login")
    public Mono<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for email: {}", request.email());
        return authService.login(request);
    }

    /**
     * Get current user info
     * Gateway validated JWT and passed X-User-Id
     */
    @GetMapping("/me")
    public Mono<UserResponse> getCurrentUser(@RequestHeader("X-User-Id") Long userId) {
        log.info("Get current user: {}", userId);
        return userService.findById(userId);
    }

    /**
     * Create user with specific role
     * Only ADMIN role can create users
     */
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        if (role == null || !role.equals("ROLE_ADMIN")) {
            return Mono.error(new IllegalArgumentException("Only admins can create users"));
        }

        log.info("Admin {} creating user with email: {} and role: {}",
                adminId, request.email(), request.role());
        return userService.createUser(request);
    }
}