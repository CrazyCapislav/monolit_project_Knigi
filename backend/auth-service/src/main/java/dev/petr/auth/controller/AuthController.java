package dev.petr.auth.controller;

import dev.petr.auth.dto.*;
import dev.petr.auth.security.JwtUtil;
import dev.petr.auth.service.AuthService;
import dev.petr.auth.service.UserService;
import io.jsonwebtoken.Claims;
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
    private final JwtUtil jwtUtil;

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
     * Get current user info from JWT token in Authorization header
     * Gateway passes request without modification - we parse JWT ourselves
     */
    @GetMapping("/me")
    public Mono<UserResponse> getCurrentUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        log.info("Get current user from JWT token");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing or invalid Authorization header");
            return Mono.error(new IllegalArgumentException("Authorization header required"));
        }

        try {
            String token = authHeader.substring(7);

            if (jwtUtil.isNotValid(token)) {
                log.warn("Invalid JWT token");
                log.warn(token, jwtUtil.getExpirationMillis().toString());
                return Mono.error(new IllegalArgumentException("Invalid token"));
            }

            Claims claims = jwtUtil.extractClaims(token);
            Long userId = Long.parseLong(claims.getSubject());

            log.info("Extracted userId from JWT: {}", userId);
            return userService.findById(userId);
        } catch (NumberFormatException e) {
            log.error("Invalid userId in token", e);
            return Mono.error(new IllegalArgumentException("Invalid token format"));
        } catch (Exception e) {
            log.error("Error parsing JWT token", e);
            return Mono.error(new IllegalArgumentException("Invalid token"));
        }
    }

    /**
     * Create user with specific role (admin only)
     * Gateway passes request without modification - we parse JWT ourselves
     */
    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        log.info("Create user request for email: {}", request.email());

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.error(new IllegalArgumentException("Authorization header required"));
        }

        try {
            String token = authHeader.substring(7);

            if (jwtUtil.isNotValid(token)) {
                return Mono.error(new IllegalArgumentException("Invalid token"));
            }

            Claims claims = jwtUtil.extractClaims(token);
            String role = claims.get("role", String.class);

            if (!"ADMIN".equals(role)) {
                log.warn("Non-admin user attempted to create user. Role: {}", role);
                return Mono.error(new IllegalArgumentException("Only admins can create users"));
            }

            log.info("Admin creating user with email: {} and role: {}", request.email(), request.role());
            return userService.createUser(request);
        } catch (Exception e) {
            log.error("Error validating admin token", e);
            return Mono.error(new IllegalArgumentException("Invalid token"));
        }
    }
}