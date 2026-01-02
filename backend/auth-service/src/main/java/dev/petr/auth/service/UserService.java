package dev.petr.auth.service;

import dev.petr.auth.dto.CreateUserRequest;
import dev.petr.auth.dto.RegisterRequest;
import dev.petr.auth.dto.UserResponse;
import dev.petr.auth.entity.Role;
import dev.petr.auth.entity.User;
import dev.petr.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<UserResponse> register(RegisterRequest request) {
        return userRepository.existsByEmail(request.email())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("Email already registered"));
                    }
                    
                    User user = User.builder()
                            .email(request.email())
                            .passwordHash(passwordEncoder.encode(request.password()))
                            .displayName(request.displayName())
                            .role(Role.USER)  // Default role
                            .createdAt(OffsetDateTime.now())
                            .build();
                    
                    return userRepository.save(user);
                })
                .map(this::toResponse);
    }

    public Mono<UserResponse> createUser(CreateUserRequest request) {
        return userRepository.existsByEmail(request.email())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("Email already registered"));
                    }
                    
                    Role role;
                    try {
                        role = Role.valueOf(request.role().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return Mono.error(new IllegalArgumentException("Invalid role: " + request.role()));
                    }
                    
                    User user = User.builder()
                            .email(request.email())
                            .passwordHash(passwordEncoder.encode(request.password()))
                            .displayName(request.displayName())
                            .role(role)
                            .createdAt(OffsetDateTime.now())
                            .build();
                    
                    return userRepository.save(user);
                })
                .map(this::toResponse);
    }

    public Mono<UserResponse> findById(Long id) {
        return userRepository.findById(id)
                .map(this::toResponse)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found")));
    }

    public Mono<User> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found")));
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole().name()
        );
    }
}
