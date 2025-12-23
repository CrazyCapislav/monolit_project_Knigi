package dev.petr.auth.service;

import dev.petr.auth.dto.CreateUserRequest;
import dev.petr.auth.dto.RegisterRequest;
import dev.petr.auth.dto.UserResponse;
import dev.petr.auth.entity.Role;
import dev.petr.auth.entity.User;
import dev.petr.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .displayName("Test User")
                .passwordHash("hashedPassword")
                .role(Role.USER)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest("new@test.com", "New User", "password123");

        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));

        Mono<UserResponse> result = userService.register(request);

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.email().equals(testUser.getEmail()) &&
                                response.role().equals("USER")
                )
                .verifyComplete();

        verify(userRepository).existsByEmail("new@test.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_EmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("existing@test.com", "User", "password");
        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(true));

        Mono<UserResponse> result = userService.register(request);

        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_WithAdminRole_Success() {
        CreateUserRequest request = new CreateUserRequest(
                "admin@test.com",
                "Admin User",
                "password",
                "ADMIN"
        );

        User adminUser = User.builder()
                .id(2L)
                .email("admin@test.com")
                .displayName("Admin User")
                .passwordHash("hashedPassword")
                .role(Role.ADMIN)
                .createdAt(OffsetDateTime.now())
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(adminUser));

        Mono<UserResponse> result = userService.createUser(request);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.role().equals("ADMIN"))
                .verifyComplete();
    }

    @Test
    void createUser_InvalidRole_ThrowsException() {
        CreateUserRequest request = new CreateUserRequest(
                "user@test.com",
                "User",
                "password",
                "INVALID_ROLE"
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(Mono.just(false));

        Mono<UserResponse> result = userService.createUser(request);

        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void findById_UserExists() {
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));

        Mono<UserResponse> result = userService.findById(1L);

        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.id().equals(1L) &&
                                response.email().equals("test@test.com")
                )
                .verifyComplete();
    }

    @Test
    void findById_UserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Mono.empty());

        Mono<UserResponse> result = userService.findById(999L);

        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void verifyPassword_CorrectPassword() {
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);

        boolean result = userService.verifyPassword("password123", "hashedPassword");

        assert result;
    }

    @Test
    void verifyPassword_IncorrectPassword() {
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        boolean result = userService.verifyPassword("wrongPassword", "hashedPassword");

        assert !result;
    }

    @Test
    void findByEmail_UserExists() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Mono.just(testUser));

        Mono<User> result = userService.findByEmail("test@test.com");

        StepVerifier.create(result)
                .expectNextMatches(user -> user.getEmail().equals("test@test.com"))
                .verifyComplete();
    }

    @Test
    void findByEmail_UserNotFound() {
        when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Mono.empty());

        Mono<User> result = userService.findByEmail("nonexistent@test.com");

        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}