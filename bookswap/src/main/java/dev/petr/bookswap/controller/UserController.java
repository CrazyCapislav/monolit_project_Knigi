package dev.petr.bookswap.controller;

import dev.petr.bookswap.dto.*;
import dev.petr.bookswap.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController @RequestMapping("/api/v1/users")
public class UserController {
    private final UserService service;
    public UserController(UserService service){ this.service = service; }

    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserCreateRequest req){
        UserResponse r = service.register(req);
        return ResponseEntity.created(URI.create("/api/v1/users/"+r.id())).body(r);
    }

    @GetMapping("/me")
    public UserResponse me(@RequestHeader("X-User-Id") Long id){
        return service.findById(id);
    }
}
