package dev.petr.bookswap.service;

import dev.petr.bookswap.dto.*;
import dev.petr.bookswap.entity.Role;
import dev.petr.bookswap.entity.User;
import dev.petr.bookswap.exception.NotFoundException;
import dev.petr.bookswap.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class UserService {
    private final UserRepository repo;
    public UserService(UserRepository repo){ this.repo = repo; }

    public UserResponse register(UserCreateRequest req){
        User u = User.builder()
                .email(req.email())
                .passwordHash(req.password())  // TODO: hash in lab-2
                .displayName(req.displayName())
                .role(Role.USER)
                .createdAt(OffsetDateTime.now())
                .build();
        return toResponse(repo.save(u));
    }

    public UserResponse findById(Long id){
        return repo.findById(id).map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
    private UserResponse toResponse(User u){
        return new UserResponse(u.getId(),u.getEmail(),u.getDisplayName(),u.getRole().name());
    }
}
