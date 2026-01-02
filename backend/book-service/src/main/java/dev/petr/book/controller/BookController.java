package dev.petr.book.controller;

import dev.petr.book.dto.BookCreateRequest;
import dev.petr.book.dto.BookResponse;
import dev.petr.book.dto.UpdateBookOwnerRequest;
import dev.petr.book.service.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * Create a new book
     * USER, ADMIN, PUBLISHER can create books
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BookResponse> create(
            @RequestHeader("X-User-Id") Long ownerId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody BookCreateRequest request
    ) {
        if (role == null || (!role.equals("ROLE_USER") && !role.equals("ROLE_ADMIN") && !role.equals("ROLE_PUBLISHER"))) {
            return Mono.error(new IllegalArgumentException("Access denied"));
        }

        log.info("User {} (role: {}) creating book: {}", ownerId, role, request.title());
        return bookService.create(ownerId, request);
    }

    @GetMapping("/{id}")
    public Mono<BookResponse> getById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId
    ) {
        log.debug("User {} viewing book {}", userId, id);
        return bookService.findById(id);
    }

    @GetMapping
    public Mono<ResponseEntity<Flux<BookResponse>>> page(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        return bookService.findAll(page, size)
                .map(p -> ResponseEntity.ok()
                        .header("X-Total-Count", String.valueOf(p.getTotalElements()))
                        .body(Flux.fromIterable(p.getContent())));
    }

    @GetMapping("/feed")
    public Flux<BookResponse> feed(
            @RequestParam(required = false) Long afterId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit
    ) {
        return bookService.feed(afterId, limit);
    }

    @GetMapping("/mine")
    public Flux<BookResponse> getMyBooks(@RequestHeader("X-User-Id") Long userId) {
        log.debug("User {} fetching own books", userId);
        return bookService.findByOwnerId(userId);
    }

    /**
     * Delete a book
     * USER can delete only their own books
     * ADMIN can delete any book
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long ownerId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        log.info("User {} (role: {}) deleting book {}", ownerId, role, id);

        if (role != null && role.equals("ROLE_ADMIN")) {
            return bookService.deleteByAdmin(id);
        }

        if (role != null && role.equals("ROLE_USER")) {
            return bookService.delete(id, ownerId);
        }

        return Mono.error(new IllegalArgumentException("Access denied"));
    }

    /**
     * Update book owner (used during exchanges)
     * Only USER can transfer ownership
     */
    @PutMapping("/{id}/owner")
    public Mono<BookResponse> updateOwner(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookOwnerRequest request,
            @RequestHeader("X-User-Id") Long currentOwnerId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        if (role == null || !role.equals("ROLE_USER")) {
            return Mono.error(new IllegalArgumentException("Only users can transfer book ownership"));
        }

        log.info("User {} transferring book {} to user {}",
                currentOwnerId, id, request.newOwnerId());
        return bookService.updateOwner(id, currentOwnerId, request.newOwnerId());
    }
}