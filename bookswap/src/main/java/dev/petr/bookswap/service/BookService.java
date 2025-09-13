package dev.petr.bookswap.service;

import dev.petr.bookswap.dto.BookCreateRequest;
import dev.petr.bookswap.dto.BookResponse;
import dev.petr.bookswap.entity.*;
import dev.petr.bookswap.exception.NotFoundException;
import dev.petr.bookswap.repository.BookRepository;
import dev.petr.bookswap.repository.GenreRepository;
import dev.petr.bookswap.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepo;
    private final UserRepository userRepo;
    private final GenreRepository genreRepo;

    public BookService(BookRepository bookRepo, UserRepository userRepo, GenreRepository genreRepo) {
        this.bookRepo = bookRepo;
        this.userRepo = userRepo;
        this.genreRepo = genreRepo;
    }

    @Transactional
    public BookResponse create(Long ownerId, BookCreateRequest req) {
        User owner = userRepo.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("User not found: " + ownerId));

        Set<Genre> genres = req.genreIds() == null || req.genreIds().isEmpty()
                ? Set.of()
                : genreRepo.findAllById(req.genreIds()).stream().collect(Collectors.toSet());

        Book book = Book.builder()
                .title(req.title())
                .author(req.author())
                .isbn(req.isbn())
                .publishedYear(req.publishedYear())
                .owner(owner)
                .status(BookStatus.AVAILABLE)
                .condition(BookCondition.valueOf(req.condition()))
                .createdAt(OffsetDateTime.now())
                .genres(genres)
                .build();

        Book saved = bookRepo.save(book);
        return toResponse(saved);
    }

    public Page<BookResponse> findAll(int page, int size) {
        int pageSize = Math.min(size <= 0 ? 20 : size, 50); // ограничение <= 50
        Page<Book> p = bookRepo.findAll(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "id")));
        return p.map(this::toResponse);
    }

    public java.util.List<BookResponse> feed(Long afterId, int limit) {
        int l = Math.min(limit <= 0 ? 20 : limit, 50);
        Long cursor = (afterId == null || afterId <= 0) ? Long.MAX_VALUE : afterId;
        return bookRepo.findTop50ByIdLessThanOrderByIdDesc(cursor)
                .stream().limit(l).map(this::toResponse).toList();
    }

    private BookResponse toResponse(Book b) {
        return new BookResponse(
                b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(), b.getPublishedYear(),
                b.getStatus().name(), b.getCondition().name(),
                b.getCreatedAt(),
                b.getOwner().getId(),
                b.getGenres() == null ? Set.of() : b.getGenres().stream().map(Genre::getName).collect(Collectors.toSet())
        );
    }
}
