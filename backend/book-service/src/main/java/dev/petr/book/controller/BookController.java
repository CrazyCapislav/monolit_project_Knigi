package dev.petr.book.controller;

import dev.petr.book.dto.BookCreateRequest;
import dev.petr.book.dto.BookResponse;
import dev.petr.book.dto.UpdateBookOwnerRequest;
import dev.petr.book.service.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BookResponse> create(
            @RequestHeader("X-User-Id") Long ownerId,
            @Valid @RequestBody BookCreateRequest request
    ) {
        return bookService.create(ownerId, request);
    }

    @GetMapping("/{id}")
    public Mono<BookResponse> getById(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId
    ) {
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

    @GetMapping("/mybooks")
    public Flux<BookResponse> getMyBooks(@RequestHeader("X-User-Id") Long userId) {
        return bookService.findByOwnerId(userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long ownerId
    ) {
        return bookService.delete(id, ownerId);
    }

    /**
     * Update book owner (for exchanges)
     */
    @PutMapping("/{id}/owner")
    public Mono<BookResponse> updateOwner(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookOwnerRequest request,
            @RequestHeader("X-User-Id") Long currentOwnerId
    ) {
        return bookService.updateOwner(id, currentOwnerId, request.newOwnerId());
    }
}
