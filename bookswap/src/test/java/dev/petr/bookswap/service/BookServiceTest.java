package dev.petr.bookswap.service;

import dev.petr.bookswap.dto.BookCreateRequest;
import dev.petr.bookswap.entity.*;
import dev.petr.bookswap.exception.NotFoundException;
import dev.petr.bookswap.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock BookRepository  bookRepo;
    @Mock UserService     userService;
    @Mock GenreService    genreService;

    @InjectMocks BookService service;

    @Test
    void shouldCreateBook() {
        User owner = User.builder().id(1L).build();
        when(userService.getEntity(1L)).thenReturn(owner);
        var genres = Set.of(new Genre(5L, "Sci-Fi"));
        when(genreService.getEntities(Set.of(5L))).thenReturn(genres);
        when(bookRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        BookCreateRequest req = new BookCreateRequest(
                "Dune", "Frank Herbert", null, 1965, "GOOD", Set.of(5L));

        var resp = service.create(1L, req);

        assertThat(resp.genres()).containsExactly("Sci-Fi");
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userService.getEntity(anyLong()))
                .thenThrow(new NotFoundException("User not found"));

        BookCreateRequest req = new BookCreateRequest(
                "x", "y", null, 2000, "GOOD", Set.of());

        assertThatThrownBy(() -> service.create(99L, req))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldLimitPageSizeTo50() {
        when(bookRepo.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());

        service.findAll(0, 200);

        ArgumentCaptor<Pageable> cap = ArgumentCaptor.forClass(Pageable.class);
        verify(bookRepo).findAll(cap.capture());
        assertThat(cap.getValue().getPageSize()).isEqualTo(50);
    }

    @Test
    void shouldGetBookEntity() {
        Book book = Book.builder()
                .id(1L)
                .title("Test Book")
                .author("Author")
                .status(BookStatus.AVAILABLE)
                .condition(BookCondition.GOOD)
                .createdAt(OffsetDateTime.now())
                .build();

        when(bookRepo.findById(1L)).thenReturn(Optional.of(book));

        Book result = service.getEntity(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Book");
    }

    @Test
    void shouldThrowWhenBookNotFound() {
        when(bookRepo.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEntity(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Book not found");
    }

    @Test
    void shouldReturnFeed() {
        Book book1 = Book.builder()
                .id(10L)
                .title("Book1")
                .author("Author1")
                .status(BookStatus.AVAILABLE)
                .condition(BookCondition.GOOD)
                .createdAt(OffsetDateTime.now())
                .owner(User.builder().id(1L).build())
                .genres(Set.of())
                .build();

        when(bookRepo.findTop50ByIdLessThanFetchGenres(anyLong(), any(Pageable.class)))
                .thenReturn(List.of(book1));

        var feed = service.feed(100L, 20);

        assertThat(feed).hasSize(1);
        assertThat(feed.get(0).id()).isEqualTo(10L);
    }

    @Test
    void shouldLimitFeedSizeTo50() {
        when(bookRepo.findTop50ByIdLessThanFetchGenres(anyLong(), any(Pageable.class)))
                .thenReturn(List.of());

        service.feed(null, 200);

        ArgumentCaptor<Pageable> cap = ArgumentCaptor.forClass(Pageable.class);
        verify(bookRepo).findTop50ByIdLessThanFetchGenres(anyLong(), cap.capture());
        assertThat(cap.getValue().getPageSize()).isEqualTo(50);
    }
}
