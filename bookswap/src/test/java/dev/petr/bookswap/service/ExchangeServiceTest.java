package dev.petr.bookswap.service;

import dev.petr.bookswap.dto.ExchangeRequestCreateRequest;
import dev.petr.bookswap.entity.*;
import dev.petr.bookswap.exception.NotFoundException;
import dev.petr.bookswap.repository.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExchangeServiceTest {

    @Mock ExchangeRequestRepository repo;
    @Mock BookRepository bookRepo;
    @Mock UserRepository userRepo;
    @InjectMocks ExchangeService service;

    User requester = User.builder().id(1L).build();
    User owner     = User.builder().id(2L).build();
    Book book      = Book.builder().id(10L).owner(owner)
            .status(BookStatus.AVAILABLE)
            .condition(BookCondition.GOOD)
            .createdAt(OffsetDateTime.now()).build();

    ExchangeServiceTest() { MockitoAnnotations.openMocks(this); }

    @Test void create_ok() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(requester));
        when(bookRepo.findById(10L)).thenReturn(Optional.of(book));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        var resp = service.create(1L, new ExchangeRequestCreateRequest(10L, null));

        assertThat(resp.status()).isEqualTo("WAITING");
    }

    @Test void accept_exchange() {
        ExchangeRequest er = ExchangeRequest.builder()
                .id(5L).requester(requester).owner(owner)
                .bookRequested(book).status(ExchangeStatus.WAITING)
                .createdAt(OffsetDateTime.now()).build();
        when(repo.findById(5L)).thenReturn(Optional.of(er));

        var resp = service.accept(5L, 2L);

        assertThat(resp.status()).isEqualTo("ACCEPTED");
        assertThat(book.getStatus()).isEqualTo(BookStatus.EXCHANGED);
    }

    @Test void accept_wrongOwner() {
        ExchangeRequest er = ExchangeRequest.builder()
                .id(5L).requester(requester).owner(owner)
                .bookRequested(book).status(ExchangeStatus.WAITING)
                .createdAt(OffsetDateTime.now()).build();
        when(repo.findById(5L)).thenReturn(Optional.of(er));

        assertThatThrownBy(() -> service.accept(5L, 99L))
                .isInstanceOf(IllegalStateException.class);
    }
}
