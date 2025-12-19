package dev.petr.auth.controller;

import dev.petr.auth.dto.*;
import dev.petr.auth.entity.Role;
import dev.petr.auth.security.JwtUtil;
import dev.petr.auth.service.AuthService;
import dev.petr.auth.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest("test@test.com", "Test User", "password123");
        UserResponse response = new UserResponse(1L, "test@test.com", "Test User", "USER");

        when(userService.register(any(RegisterRequest.class))).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.email").isEqualTo("test@test.com")
                .jsonPath("$.role").isEqualTo("USER");
    }

    @Test
    void register_ValidationFailed() {
        RegisterRequest request = new RegisterRequest("", "", "");

        webTestClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("test@test.com", "password123");
        UserResponse userResponse = new UserResponse(1L, "test@test.com", "Test User", "USER");
        LoginResponse response = new LoginResponse("jwt-token", 3600000L, userResponse);

        when(authService.login(any(LoginRequest.class))).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.token").isEqualTo("jwt-token")
                .jsonPath("$.user.email").isEqualTo("test@test.com");
    }

    @Test
    void login_InvalidCredentials() {
        LoginRequest request = new LoginRequest("test@test.com", "wrongPassword");

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(Mono.error(new IllegalArgumentException("Invalid credentials")));

        webTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getCurrentUser_ValidToken() {
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("sub", "1");
        Claims claims = Jwts.claims(claimsMap);
        
        UserResponse response = new UserResponse(1L, "test@test.com", "Test User", "USER");

        when(jwtUtil.isNotValid(anyString())).thenReturn(false);
        when(jwtUtil.extractClaims(anyString())).thenReturn(claims);
        when(userService.findById(1L)).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri("/api/v1/auth/me")
                .header("Authorization", "Bearer valid-token")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.email").isEqualTo("test@test.com");
    }

    @Test
    void getCurrentUser_MissingAuthHeader() {
        webTestClient.get()
                .uri("/api/v1/auth/me")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getCurrentUser_InvalidAuthHeader() {
        webTestClient.get()
                .uri("/api/v1/auth/me")
                .header("Authorization", "InvalidFormat")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getCurrentUser_InvalidToken() {
        when(jwtUtil.isNotValid(anyString())).thenReturn(true);

        webTestClient.get()
                .uri("/api/v1/auth/me")
                .header("Authorization", "Bearer invalid-token")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createUser_AsAdmin_Success() {
        CreateUserRequest request = new CreateUserRequest(
                "newuser@test.com",
                "New User",
                "password123",
                "PUBLISHER"
        );
        
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("sub", "1");
        claimsMap.put("role", "ADMIN");
        Claims claims = Jwts.claims(claimsMap);
        
        UserResponse response = new UserResponse(2L, "newuser@test.com", "New User", "PUBLISHER");

        when(jwtUtil.isNotValid(anyString())).thenReturn(false);
        when(jwtUtil.extractClaims(anyString())).thenReturn(claims);
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/v1/auth/users")
                .header("Authorization", "Bearer admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.email").isEqualTo("newuser@test.com")
                .jsonPath("$.role").isEqualTo("PUBLISHER");
    }

    @Test
    void createUser_AsNonAdmin_Forbidden() {
        CreateUserRequest request = new CreateUserRequest(
                "newuser@test.com",
                "New User",
                "password123",
                "ADMIN"
        );
        
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("sub", "1");
        claimsMap.put("role", "USER");
        Claims claims = Jwts.claims(claimsMap);

        when(jwtUtil.isNotValid(anyString())).thenReturn(false);
        when(jwtUtil.extractClaims(anyString())).thenReturn(claims);

        webTestClient.post()
                .uri("/api/v1/auth/users")
                .header("Authorization", "Bearer user-token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createUser_MissingAuthHeader() {
        CreateUserRequest request = new CreateUserRequest(
                "newuser@test.com",
                "New User",
                "password123",
                "USER"
        );

        webTestClient.post()
                .uri("/api/v1/auth/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }
}

