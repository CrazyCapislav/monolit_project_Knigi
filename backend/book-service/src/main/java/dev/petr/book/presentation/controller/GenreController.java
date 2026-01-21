package dev.petr.book.presentation.controller;

import dev.petr.book.application.dto.GenreCreateRequest;
import dev.petr.book.application.dto.GenreResponse;
import dev.petr.book.application.service.GenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    /**
     * Create a new genre
     * Only ADMIN can create genres
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GenreResponse> create(
            @Valid @RequestBody GenreCreateRequest request,
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        if (role == null || !role.equals("ROLE_ADMIN")) {
            return Mono.error(new IllegalArgumentException("Only admins can create genres"));
        }

        log.info("Admin {} creating genre: {}", adminId, request.name());
        return genreService.create(request);
    }

    /**
     * Get all genres - доступно всем аутентифицированным пользователям
     */
    @GetMapping
    public Flux<GenreResponse> getAll() {
        return genreService.findAll();
    }

    /**
     * Delete a genre
     * Only ADMIN can delete genres
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long adminId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        if (role == null || !role.equals("ROLE_ADMIN")) {
            return Mono.error(new IllegalArgumentException("Only admins can delete genres"));
        }

        log.info("Admin {} deleting genre {}", adminId, id);
        return genreService.delete(id);
    }
}
