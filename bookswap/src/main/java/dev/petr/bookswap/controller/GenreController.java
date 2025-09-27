package dev.petr.bookswap.controller;

import dev.petr.bookswap.dto.GenreCreateRequest;
import dev.petr.bookswap.dto.GenreResponse;
import dev.petr.bookswap.service.GenreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService service;

    @Operation(
            summary = "Создать жанр",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = GenreCreateRequest.class))
            )
    )
    @PostMapping
    public ResponseEntity<GenreResponse> create(
            @RequestBody @Valid
            GenreCreateRequest req
    ) {
        GenreResponse created = service.create(req);
        return ResponseEntity.created(URI.create("/api/v1/genres/" + created.id()))
                .body(created);
    }

    @Operation(summary = "Список всех жанров (по алфавиту)")
    @GetMapping
    public List<GenreResponse> getAll() {
        return service.findAll();
    }

    @Operation(summary = "Удалить жанр")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
