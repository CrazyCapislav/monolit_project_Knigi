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
}
