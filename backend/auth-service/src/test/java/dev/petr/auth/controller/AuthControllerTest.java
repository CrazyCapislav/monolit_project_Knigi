package dev.petr.auth.controller;

import dev.petr.auth.dto.*;
import dev.petr.auth.service.AuthService;
import dev.petr.auth.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

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
        LoginResponse response = new LoginResponse(userResponse);

        when(authService.login(any(LoginRequest.class))).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
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
    void getCurrentUser_ValidHeader() {
        UserResponse response = new UserResponse(1L, "test@test.com", "Test User", "USER");

        when(userService.findById(1L)).thenReturn(Mono.just(response));

        webTestClient.get()
                .uri("/api/v1/auth/me")
                .header("X-User-Id", "1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.email").isEqualTo("test@test.com");
    }

    @Test
    void getCurrentUser_MissingHeader() {
        webTestClient.get()
                .uri("/api/v1/auth/me")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void getCurrentUser_InvalidUserId() {
        webTestClient.get()
                .uri("/api/v1/auth/me")
                .header("X-User-Id", "invalid")
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
        
        UserResponse response = new UserResponse(2L, "newuser@test.com", "New User", "PUBLISHER");

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/v1/auth/users")
                .header("X-User-Role", "ADMIN")
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

        webTestClient.post()
                .uri("/api/v1/auth/users")
                .header("X-User-Role", "USER")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void createUser_MissingHeader() {
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

