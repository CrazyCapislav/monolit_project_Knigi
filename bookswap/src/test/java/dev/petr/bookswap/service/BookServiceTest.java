package dev.petr.bookswap.service;

import dev.petr.bookswap.dto.BookCreateRequest;
import dev.petr.bookswap.entity.*;
import dev.petr.bookswap.exception.NotFoundException;
import dev.petr.bookswap.repository.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookServiceTest {

    @Mock UserRepository userRepo;
    @Mock BookRepository bookRepo;
    @Mock GenreRepository genreRepo;

    @InjectMocks BookService service;

    BookServiceTest() { MockitoAnnotations.openMocks(this); }

    @Test void create_ok() {
        User u = User.builder().id(1L).build();
        when(userRepo.findById(1L)).thenReturn(Optional.of(u));
        when(genreRepo.findAllById(any())).thenReturn(List.of());
        when(bookRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        var req = new BookCreateRequest(
                "Dune", "Frank", null, 1965, "GOOD", Set.of());

        var resp = service.create(1L, req);

        assertThat(resp.title()).isEqualTo("Dune");
        verify(bookRepo).save(any(Book.class));
    }

    @Test void create_unknownUser() {
        when(userRepo.findById(anyLong())).thenReturn(Optional.empty());
        var req = new BookCreateRequest("t", "a", null, null, "GOOD", Set.of());
        assertThatThrownBy(() -> service.create(99L, req))
                .isInstanceOf(NotFoundException.class);
    }

    @Test void findAll_limitedTo50() {
        when(bookRepo.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());

        service.findAll(0, 200);

        verify(bookRepo).findAll(argThat((Pageable p) -> p.getPageSize() == 50));
    }
}
