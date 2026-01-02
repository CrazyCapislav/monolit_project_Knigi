package dev.petr.book.service;

import dev.petr.book.dto.BookCreateRequest;
import dev.petr.book.dto.BookResponse;
import dev.petr.book.entity.Book;
import dev.petr.book.entity.BookCondition;
import dev.petr.book.entity.BookStatus;
import dev.petr.book.entity.Genre;
import dev.petr.book.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private GenreService genreService;

    @InjectMocks
    private BookService bookService;

    @Test
    void create_Success() {
        
        BookCreateRequest request = new BookCreateRequest(
                "New Book",
                "Author",
                "978-1234567890",
                2024,
                "GOOD",
                Set.of(1L)
        );

        Genre genre = Genre.builder().id(1L).name("Fiction").build();
        Set<Genre> genres = new HashSet<>(Set.of(genre));

        Book savedBook = Book.builder()
                .id(1L)
                .title("New Book")
                .author("Author")
                .ownerId(1L)
                .status(BookStatus.AVAILABLE)
                .condition(BookCondition.GOOD)
                .createdAt(OffsetDateTime.now())
                .genres(genres)
                .build();

        when(genreService.getEntities(anySet())).thenReturn(Mono.just(genres));
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        bookService = new BookService(bookRepository, genreService, Schedulers.immediate());

        
        Mono<BookResponse> result = bookService.create(1L, request);

        
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.title().equals("New Book") &&
                                response.ownerId().equals(1L) &&
                                response.status().equals("AVAILABLE")
                )
                .verifyComplete();

        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void findById_BookExists() {
        
        Genre genre = Genre.builder().id(1L).name("Fiction").build();
        Book book = Book.builder()
                .id(1L)
                .title("Test Book")
                .author("Author")
                .ownerId(1L)
                .status(BookStatus.AVAILABLE)
                .condition(BookCondition.GOOD)
                .createdAt(OffsetDateTime.now())
                .genres(Set.of(genre))
                .build();

        when(bookRepository.findByIdWithGenres(1L)).thenReturn(Optional.of(book));

        bookService = new BookService(bookRepository, genreService, Schedulers.immediate());

        
        Mono<BookResponse> result = bookService.findById(1L);

        
        StepVerifier.create(result)
                .expectNextMatches(response ->
                        response.id().equals(1L) &&
                                response.title().equals("Test Book")
                )
                .verifyComplete();
    }

    @Test
    void findById_BookNotFound() {
        
        when(bookRepository.findByIdWithGenres(999L)).thenReturn(Optional.empty());
        bookService = new BookService(bookRepository, genreService, Schedulers.immediate());

        
        Mono<BookResponse> result = bookService.findById(999L);

        
        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void delete_AsOwner_Success() {
        
        Book book = Book.builder()
                .id(1L)
                .title("Test")
                .author("Author")
                .ownerId(1L)
                .status(BookStatus.AVAILABLE)
                .condition(BookCondition.GOOD)
                .createdAt(OffsetDateTime.now())
                .genres(Collections.emptySet())
                .build();

        when(bookRepository.findByIdWithGenres(1L)).thenReturn(Optional.of(book));
        doNothing().when(bookRepository).delete(any(Book.class));

        bookService = new BookService(bookRepository, genreService, Schedulers.immediate());

        
        Mono<Void> result = bookService.delete(1L, 1L);

        
        StepVerifier.create(result)
                .verifyComplete();

        verify(bookRepository).delete(book);
    }

    @Test
    void delete_NotOwner_ThrowsException() {
        
        Book book = Book.builder()
                .id(1L)
                .title("Test")
                .author("Author")
                .ownerId(1L)
                .status(BookStatus.AVAILABLE)
                .condition(BookCondition.GOOD)
                .createdAt(OffsetDateTime.now())
                .genres(Collections.emptySet())
                .build();

        when(bookRepository.findByIdWithGenres(1L)).thenReturn(Optional.of(book));
        bookService = new BookService(bookRepository, genreService, Schedulers.immediate());

        
        Mono<Void> result = bookService.delete(1L, 999L);

        
        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(bookRepository, never()).delete(any());
    }

    @Test
    void updateOwner_Success() {
        
        Book book = Book.builder()
                .id(1L)
                .title("Test")
                .author("Author")
                .ownerId(1L)
                .status(BookStatus.AVAILABLE)
                .condition(BookCondition.GOOD)
                .createdAt(OffsetDateTime.now())
                .genres(Collections.emptySet())
                .build();

        when(bookRepository.findByIdWithGenres(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book b = invocation.getArgument(0);
            b.setOwnerId(2L);
            return b;
        });

        bookService = new BookService(bookRepository, genreService, Schedulers.immediate());

        
        Mono<BookResponse> result = bookService.updateOwner(1L, 1L, 2L);

        
        StepVerifier.create(result)
                .expectNextMatches(response -> response.ownerId().equals(2L))
                .verifyComplete();

        verify(bookRepository).save(argThat(b ->
                b.getOwnerId().equals(2L) && b.getUpdatedAt() != null
        ));
    }

    @Test
    void updateOwner_NotCurrentOwner_ThrowsException() {
        
        Book book = Book.builder()
                .id(1L)
                .title("Test")
                .author("Author")
                .ownerId(1L)
                .status(BookStatus.AVAILABLE)
                .condition(BookCondition.GOOD)
                .createdAt(OffsetDateTime.now())
                .genres(Collections.emptySet())
                .build();

        when(bookRepository.findByIdWithGenres(1L)).thenReturn(Optional.of(book));
        bookService = new BookService(bookRepository, genreService, Schedulers.immediate());

        
        Mono<BookResponse> result = bookService.updateOwner(1L, 999L, 2L);

        
        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();

        verify(bookRepository, never()).save(any());
    }

    @Test
    void findAll_ReturnsPage() {
        
        Genre genre = Genre.builder().id(1L).name("Fiction").build();
        Book book = Book.builder()
                .id(1L)
                .title("Test")
                .author("Author")
                .ownerId(1L)
                .status(BookStatus.AVAILABLE)
                .condition(BookCondition.GOOD)
                .createdAt(OffsetDateTime.now())
                .genres(Set.of(genre))
                .build();

        Page<Book> page = new PageImpl<>(List.of(book), PageRequest.of(0, 20), 1);
        when(bookRepository.findAllWithGenres(any(Pageable.class))).thenReturn(page);

        bookService = new BookService(bookRepository, genreService, Schedulers.immediate());

        
        Mono<Page<BookResponse>> result = bookService.findAll(0, 20);

        
        StepVerifier.create(result)
                .expectNextMatches(p -> p.getTotalElements() == 1)
                .verifyComplete();
    }

    @Test
    void findByOwnerId_ReturnsBooks() {
        
        Book book = Book.builder()
                .id(1L)
                .title("Test")
                .author("Author")
                .ownerId(1L)
                .status(BookStatus.AVAILABLE)
                .condition(BookCondition.GOOD)
                .createdAt(OffsetDateTime.now())
                .genres(Collections.emptySet())
                .build();

        when(bookRepository.findByOwnerIdWithGenres(1L)).thenReturn(List.of(book));
        bookService = new BookService(bookRepository, genreService, Schedulers.immediate());

        
        var result = bookService.findByOwnerId(1L);

        
        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void create_WithoutGenres_Success() {
        
        BookCreateRequest request = new BookCreateRequest(
                "Book Without Genres",
                "Author",
                null,
                null,
                "NEW",
                null
        );

        Book savedBook = Book.builder()
                .id(1L)
                .title("Book Without Genres")
                .author("Author")
                .ownerId(1L)
                .status(BookStatus.AVAILABLE)
                .condition(BookCondition.NEW)
                .createdAt(OffsetDateTime.now())
                .genres(Collections.emptySet())
                .build();

        when(genreService.getEntities(Collections.emptySet())).thenReturn(Mono.just(Collections.emptySet()));
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        bookService = new BookService(bookRepository, genreService, Schedulers.immediate());

        Mono<BookResponse> result = bookService.create(1L, request);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.genres().isEmpty())
                .verifyComplete();
    }
}