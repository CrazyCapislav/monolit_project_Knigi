package dev.petr.bookswap.controller;

import dev.petr.bookswap.dto.*;
import dev.petr.bookswap.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @Operation(
            summary = "Регистрация нового пользователя",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserCreateRequest.class))
            )
    )
    @PostMapping
    public ResponseEntity<UserResponse> register(
            @RequestBody @Valid
            UserCreateRequest req
    ) {
        UserResponse r = service.register(req);
        return ResponseEntity.created(URI.create("/api/v1/users/" + r.id()))
                .body(r);
    }

    @Operation(summary = "Текущий пользователь по X-User-Id")
    @GetMapping("/me")
    public UserResponse me(@RequestHeader("X-User-Id") Long id) {
        return service.findById(id);
    }
}
