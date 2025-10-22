package dev.petr.bookswap.service;

import dev.petr.bookswap.dto.BookCreateRequest;
import dev.petr.bookswap.dto.BookResponse;
import dev.petr.bookswap.entity.*;
import dev.petr.bookswap.exception.NotFoundException;
import dev.petr.bookswap.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepo;
    private final UserService userService;
    private final GenreService genreService;

    @Transactional
    public BookResponse create(Long ownerId, BookCreateRequest req) {
        User owner = userService.getEntity(ownerId);
        Set<Genre> genres = req.genreIds() == null || req.genreIds().isEmpty() ? Set.of() : genreService.getEntities(req.genreIds());

        Book book = Book.builder().title(req.title()).author(req.author()).isbn(req.isbn()).publishedYear(req.publishedYear()).owner(owner).status(BookStatus.AVAILABLE).condition(BookCondition.valueOf(req.condition())).createdAt(OffsetDateTime.now()).genres(genres).build();

        return toResponse(bookRepo.save(book));
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> findAll(int page, int size) {
        return page(page, size);
    }


    @Transactional(readOnly = true)
    public Book getEntity(Long id) {
        return bookRepo.findById(id).orElseThrow(() -> new NotFoundException("Book not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> page(int page, int size) {
        int pageSize = Math.min(size <= 0 ? 50 : size, 50);
        Page<Book> p = bookRepo.findAll(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "id")));
        return p.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public java.util.List<BookResponse> feed(Long afterId, int limit) {
        int l = Math.min(limit <= 0 ? 50 : limit, 50);
        Long cursor = (afterId == null || afterId <= 0) ? Long.MAX_VALUE : afterId;
        return bookRepo.findTop50ByIdLessThanFetchGenres(cursor, PageRequest.of(0, l)).stream().map(this::toResponse).toList();
    }

    private BookResponse toResponse(Book b) {
        return new BookResponse(b.getId(), b.getTitle(), b.getAuthor(), b.getIsbn(), b.getPublishedYear(), b.getStatus().name(), b.getCondition().name(), b.getCreatedAt(), b.getOwner().getId(), b.getGenres() == null ? Set.of() : b.getGenres().stream().map(Genre::getName).collect(Collectors.toSet()));
    }
}
