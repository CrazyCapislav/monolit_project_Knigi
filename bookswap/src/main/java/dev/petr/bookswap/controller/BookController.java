package dev.petr.bookswap.controller;

import dev.petr.bookswap.dto.BookCreateRequest;
import dev.petr.bookswap.dto.BookResponse;
import dev.petr.bookswap.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService service;

    @Operation(
            summary = "Создать новую книгу",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = BookCreateRequest.class))
            )
    )
    @PostMapping
    public ResponseEntity<BookResponse> create(
            @Parameter(description = "ID владельца книги")
            @RequestHeader("X-User-Id") Long ownerId,

            @RequestBody @Valid
            BookCreateRequest req
    ) {
        BookResponse created = service.create(ownerId, req);
        return ResponseEntity.created(URI.create("/api/v1/books/" + created.id()))
                .body(created);
    }

    @Operation(summary = "Постраничный список книг (с X-Total-Count)")
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

    @Operation(summary = "Лента книг (бесконечная прокрутка)")
    @GetMapping("/feed")
    public List<BookResponse> feed(
            @RequestParam(name = "after", required = false) Long afterId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit
    ) {
        return service.feed(afterId, limit);
    }

    @Operation(summary = "Мои книги (книги текущего пользователя)")
    @GetMapping("/mine")
    public List<BookResponse> getMyBooks(
            @Parameter(description = "ID текущего пользователя")
            @RequestHeader("X-User-Id") Long userId
    ) {
        return service.findByOwnerId(userId);
    }
}
