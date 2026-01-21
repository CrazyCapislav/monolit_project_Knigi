package dev.petr.book.application.service;

import dev.petr.book.application.dto.GenreCreateRequest;
import dev.petr.book.application.dto.GenreResponse;
import dev.petr.book.domain.model.Genre;
import dev.petr.book.infrastructure.persistence.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;
    private final Scheduler jdbcScheduler;

    @Transactional
    public Mono<GenreResponse> create(GenreCreateRequest request) {
        return Mono.fromCallable(() -> {
            List<Genre> existingGenres = genreRepository.findAllByOrderByNameAsc();
            boolean exists = existingGenres.stream()
                    .anyMatch(g -> g.getName().equalsIgnoreCase(request.name()));

            if (exists) {
                throw new IllegalArgumentException("Genre with name '" + request.name() + "' already exists");
            }

            Genre genre = Genre.builder()
                    .name(request.name())
                    .build();
            Genre saved = genreRepository.save(genre);
            return new GenreResponse(saved.getId(), saved.getName());
        }).subscribeOn(jdbcScheduler);
    }

    @Transactional(readOnly = true)
    public Flux<GenreResponse> findAll() {
        return Mono.fromCallable(genreRepository::findAllByOrderByNameAsc)
                .flatMapMany(Flux::fromIterable)
                .map(g -> new GenreResponse(g.getId(), g.getName()))
                .subscribeOn(jdbcScheduler);
    }

    @Transactional(readOnly = true)
    public Mono<Set<Genre>> getEntities(Set<Long> ids) {
        return Mono.fromCallable(() -> {
            List<Genre> genreList = genreRepository.findAllById(ids);
            return (Set<Genre>) new HashSet<Genre>(genreList);
        }).subscribeOn(jdbcScheduler);
    }

    @Transactional
    public Mono<Void> delete(Long id) {
        return Mono.fromRunnable(() -> genreRepository.deleteById(id))
                .subscribeOn(jdbcScheduler)
                .then();
    }
}

