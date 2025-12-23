package dev.petr.book.controller;

import dev.petr.book.dto.GenreCreateRequest;
import dev.petr.book.dto.GenreResponse;
import dev.petr.book.service.GenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GenreResponse> create(@Valid @RequestBody GenreCreateRequest request) {
        return genreService.create(request);
    }

    @GetMapping
    public Flux<GenreResponse> getAll() {
        return genreService.findAll();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return genreService.delete(id);
    }
}
