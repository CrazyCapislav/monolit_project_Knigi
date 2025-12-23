package dev.petr.book.service;

import dev.petr.book.dto.GenreCreateRequest;
import dev.petr.book.dto.GenreResponse;
import dev.petr.book.entity.Genre;
import dev.petr.book.repository.GenreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreServiceTest {

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private GenreService genreService;

    @Test
    void create_Success() {
        
        GenreCreateRequest request = new GenreCreateRequest("Science Fiction");
        Genre newGenre = Genre.builder()
                .id(2L)
                .name("Science Fiction")
                .build();

        when(genreRepository.findAllByOrderByNameAsc()).thenReturn(Collections.emptyList());
        when(genreRepository.save(any(Genre.class))).thenReturn(newGenre);

        genreService = new GenreService(genreRepository, Schedulers.immediate());

        
        Mono<GenreResponse> result = genreService.create(request);

        
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.id().equals(2L) &&
                                response.name().equals("Science Fiction")
                )
                .verifyComplete();

        verify(genreRepository).save(any(Genre.class));
    }

    @Test
    void create_DuplicateName_ThrowsException() {
        
        GenreCreateRequest request = new GenreCreateRequest("Fiction");
        Genre existingGenre = Genre.builder().id(1L).name("Fiction").build();

        when(genreRepository.findAllByOrderByNameAsc()).thenReturn(List.of(existingGenre));
        genreService = new GenreService(genreRepository, Schedulers.immediate());

        
        Mono<GenreResponse> result = genreService.create(request);

        
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().contains("already exists")
                )
                .verify();

        verify(genreRepository, never()).save(any());
    }

    @Test
    void create_DuplicateNameCaseInsensitive_ThrowsException() {
        
        GenreCreateRequest request = new GenreCreateRequest("FICTION");
        Genre existingGenre = Genre.builder().id(1L).name("fiction").build();

        when(genreRepository.findAllByOrderByNameAsc()).thenReturn(List.of(existingGenre));
        genreService = new GenreService(genreRepository, Schedulers.immediate());

        
        Mono<GenreResponse> result = genreService.create(request);

        
        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void findAll_ReturnsAllGenres() {
        
        Genre genre1 = Genre.builder().id(1L).name("Fiction").build();
        Genre genre2 = Genre.builder().id(2L).name("Science").build();

        when(genreRepository.findAllByOrderByNameAsc()).thenReturn(List.of(genre1, genre2));
        genreService = new GenreService(genreRepository, Schedulers.immediate());

        
        Flux<GenreResponse> result = genreService.findAll();

        
        StepVerifier.create(result)
                .expectNextMatches(response -> response.name().equals("Fiction"))
                .expectNextMatches(response -> response.name().equals("Science"))
                .verifyComplete();
    }

    @Test
    void findAll_EmptyList_ReturnsEmpty() {
        
        when(genreRepository.findAllByOrderByNameAsc()).thenReturn(Collections.emptyList());
        genreService = new GenreService(genreRepository, Schedulers.immediate());

        
        Flux<GenreResponse> result = genreService.findAll();

        
        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void getEntities_ValidIds_ReturnsGenres() {
        
        Genre genre = Genre.builder().id(1L).name("Fiction").build();
        when(genreRepository.findAllById(Set.of(1L))).thenReturn(List.of(genre));

        genreService = new GenreService(genreRepository, Schedulers.immediate());

        
        Mono<Set<Genre>> result = genreService.getEntities(Set.of(1L));

        
        StepVerifier.create(result)
                .expectNextMatches(genres ->
                        genres.size() == 1 &&
                                genres.iterator().next().getName().equals("Fiction")
                )
                .verifyComplete();
    }

    @Test
    void getEntities_EmptyIds_ReturnsEmptySet() {
        
        when(genreRepository.findAllById(Collections.emptySet())).thenReturn(Collections.emptyList());
        genreService = new GenreService(genreRepository, Schedulers.immediate());

        
        Mono<Set<Genre>> result = genreService.getEntities(Collections.emptySet());

        
        StepVerifier.create(result)
                .expectNextMatches(Set::isEmpty)
                .verifyComplete();
    }

    @Test
    void getEntities_MultipleIds_ReturnsAllGenres() {
        
        Genre genre1 = Genre.builder().id(1L).name("Fiction").build();
        Genre genre2 = Genre.builder().id(2L).name("Science").build();

        when(genreRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(genre1, genre2));
        genreService = new GenreService(genreRepository, Schedulers.immediate());

        
        Mono<Set<Genre>> result = genreService.getEntities(Set.of(1L, 2L));

        
        StepVerifier.create(result)
                .expectNextMatches(genres -> genres.size() == 2)
                .verifyComplete();
    }

    @Test
    void delete_Success() {
        
        doNothing().when(genreRepository).deleteById(1L);
        genreService = new GenreService(genreRepository, Schedulers.immediate());

        
        Mono<Void> result = genreService.delete(1L);

        
        StepVerifier.create(result)
                .verifyComplete();

        verify(genreRepository).deleteById(1L);
    }
}