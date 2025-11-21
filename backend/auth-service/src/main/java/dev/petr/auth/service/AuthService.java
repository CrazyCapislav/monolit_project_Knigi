package dev.petr.auth.service;

import dev.petr.auth.dto.LoginRequest;
import dev.petr.auth.dto.LoginResponse;
import dev.petr.auth.dto.UserResponse;
import dev.petr.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public Mono<LoginResponse> login(LoginRequest request) {
        return userService.findByEmail(request.email())
                .flatMap(user -> {
                    if (!userService.verifyPassword(request.password(), user.getPasswordHash())) {
                        return Mono.error(new IllegalArgumentException("Invalid credentials"));
                    }
                    
                    String token = jwtUtil.generateToken(user);
                    UserResponse userResponse = new UserResponse(
                            user.getId(),
                            user.getEmail(),
                            user.getDisplayName(),
                            user.getRole().name()
                    );
                    
                    return Mono.just(new LoginResponse(token, jwtUtil.getExpirationMillis(), userResponse));
                });
    }
}
