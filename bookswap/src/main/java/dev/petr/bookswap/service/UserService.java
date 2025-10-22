package dev.petr.bookswap.service;

import dev.petr.bookswap.dto.UserCreateRequest;
import dev.petr.bookswap.dto.UserResponse;
import dev.petr.bookswap.entity.Role;
import dev.petr.bookswap.entity.User;
import dev.petr.bookswap.exception.NotFoundException;
import dev.petr.bookswap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Service for managing users and authentication.
 * Handles user registration and retrieval.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;

    /**
     * Register a new user in the system.
     * Default role is USER.
     * 
     * @param req user registration request
     * @return created user response
     */
    @Transactional
    public UserResponse register(UserCreateRequest req) {
        User u = User.builder().email(req.email()).passwordHash(req.password()) // TODO: hash in lab-2
                .displayName(req.displayName()).role(Role.USER).createdAt(OffsetDateTime.now()).build();
        return toResponse(repo.save(u));
    }

    /**
     * Find user by ID.
     * 
     * @param id user ID
     * @return user response
     * @throws NotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public User getEntity(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getDisplayName(), u.getRole().name());
    }
}
