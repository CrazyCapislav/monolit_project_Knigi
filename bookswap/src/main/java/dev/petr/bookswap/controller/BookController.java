package dev.petr.bookswap.controller;

import dev.petr.bookswap.dto.BookCreateRequest;
import dev.petr.bookswap.dto.BookResponse;
import dev.petr.bookswap.service.BookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService service;
    public BookController(BookService service) { this.service = service; }

    // Пример: ownerId берём из заголовка (до security). Позже заменим на пользователя из SecurityContext.
    @PostMapping
    public ResponseEntity<BookResponse> create(
            @RequestHeader("X-User-Id") Long ownerId,
            @Valid @RequestBody BookCreateRequest req
    ) {
        BookResponse created = service.create(ownerId, req);
        return ResponseEntity.created(URI.create("/api/v1/books/" + created.id())).body(created);
    }

    // Пагинация с total count (кладём в заголовок X-Total-Count)
    @GetMapping
    public ResponseEntity<List<BookResponse>> page(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        Page<BookResponse> p = service.findAll(page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(p.getTotalElements()))
                .body(p.getContent());
    }

    // «Бесконечная прокрутка»: без общего количества, только порция и курсор
    @GetMapping("/feed")
    public List<BookResponse> feed(
            @RequestParam(name="after", required = false) Long afterId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit
    ) {
        return service.feed(afterId, limit);
    }
}
