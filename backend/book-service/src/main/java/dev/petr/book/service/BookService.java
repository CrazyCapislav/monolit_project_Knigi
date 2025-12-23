package dev.petr.book.service;

import dev.petr.book.dto.BookCreateRequest;
import dev.petr.book.dto.BookResponse;
import dev.petr.book.entity.Book;
import dev.petr.book.entity.BookCondition;
import dev.petr.book.entity.BookStatus;
import dev.petr.book.entity.Genre;
import dev.petr.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final GenreService genreService;
    private final Scheduler jdbcScheduler;

    @Transactional
    public Mono<BookResponse> create(Long ownerId, BookCreateRequest request) {
        return genreService.getEntities(request.genreIds() != null ? request.genreIds() : Collections.emptySet())
                .flatMap(genres -> Mono.fromCallable(() -> {
                    Book book = Book.builder()
                            .title(request.title())
                            .author(request.author())
                            .isbn(request.isbn())
                            .publishedYear(request.publishedYear())
                            .ownerId(ownerId)
                            .status(BookStatus.AVAILABLE)
                            .condition(BookCondition.valueOf(request.condition().toUpperCase()))
                            .createdAt(OffsetDateTime.now())
                            .genres(genres)
                            .build();
                    Book saved = bookRepository.save(book);
                    return toResponse(saved);
                }).subscribeOn(jdbcScheduler));
    }

    @Transactional(readOnly = true)
    public Mono<BookResponse> findById(Long id) {
        log.info("Finding book by id: {}", id);
        return Mono.fromCallable(() -> {
                    return bookRepository.findByIdWithGenres(id)
                            .orElseThrow(() -> new IllegalArgumentException("Book not found with id: " + id));
                })
                .map(this::toResponse)
                .subscribeOn(jdbcScheduler);
    }

    @Transactional(readOnly = true)
    public Mono<Page<BookResponse>> findAll(int page, int size) {
        return Mono.fromCallable(() -> {
            Pageable pageable = PageRequest.of(page, size);
            return bookRepository.findAllWithGenres(pageable).map(this::toResponse);
        }).subscribeOn(jdbcScheduler);
    }

    @Transactional(readOnly = true)
    public Flux<BookResponse> feed(Long afterId, int limit) {
        return Mono.fromCallable(() -> {
                    Pageable pageable = PageRequest.of(0, limit);
                    return bookRepository.findTopBooksWithGenres(afterId, pageable);
                })
                .flatMapMany(Flux::fromIterable)
                .map(this::toResponse)
                .subscribeOn(jdbcScheduler);
    }

    @Transactional(readOnly = true)
    public Flux<BookResponse> findByOwnerId(Long ownerId) {
        return Mono.fromCallable(() -> {
                    return bookRepository.findByOwnerIdWithGenres(ownerId);
                })
                .flatMapMany(Flux::fromIterable)
                .map(this::toResponse)
                .subscribeOn(jdbcScheduler);
    }

    @Transactional
    public Mono<Void> delete(Long id, Long ownerId) {
        return Mono.fromCallable(() -> {
                    return bookRepository.findByIdWithGenres(id).orElse(null);
                })
                .subscribeOn(jdbcScheduler)
                .flatMap(book -> {
                    if (book == null) {
                        return Mono.error(new IllegalArgumentException("Book not found"));
                    }
                    if (!book.getOwnerId().equals(ownerId)) {
                        return Mono.error(new IllegalArgumentException("Not the owner"));
                    }
                    return Mono.fromRunnable(() -> bookRepository.delete(book))
                            .subscribeOn(jdbcScheduler)
                            .then();
                });
    }

    @Transactional
    public Mono<BookResponse> updateOwner(Long bookId, Long currentOwnerId, Long newOwnerId) {
        log.info("Updating book {} owner from {} to {}", bookId, currentOwnerId, newOwnerId);

        return Mono.fromCallable(() -> {
            Book book = bookRepository.findByIdWithGenres(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("Book not found"));

            if (!book.getOwnerId().equals(currentOwnerId)) {
                throw new IllegalArgumentException("Only the book owner can transfer ownership");
            }

            book.setOwnerId(newOwnerId);
            book.setUpdatedAt(OffsetDateTime.now());

            Book updated = bookRepository.save(book);

            return toResponse(updated);
        }).subscribeOn(jdbcScheduler);
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPublishedYear(),
                book.getStatus().name(),
                book.getCondition().name(),
                book.getCreatedAt(),
                book.getOwnerId(),
                book.getGenres() != null && !book.getGenres().isEmpty()
                        ? book.getGenres().stream()
                        .map(Genre::getName)
                        .collect(Collectors.toSet())
                        : Collections.emptySet()
        );
    }
}