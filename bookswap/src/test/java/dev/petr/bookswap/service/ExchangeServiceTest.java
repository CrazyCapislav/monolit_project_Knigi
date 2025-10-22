package dev.petr.bookswap.service;

import dev.petr.bookswap.dto.ExchangeRequestCreateRequest;
import dev.petr.bookswap.entity.*;
import dev.petr.bookswap.exception.NotFoundException;
import dev.petr.bookswap.repository.ExchangeRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeServiceTest {

    @Mock ExchangeRequestRepository repo;
    @Mock BookService               bookService;
    @Mock UserService               userService;

    @InjectMocks ExchangeService service;

    User requester = User.builder().id(1L).build();
    User owner     = User.builder().id(2L).build();
    Book requested = Book.builder().id(10L).owner(owner)
            .status(BookStatus.AVAILABLE)
            .condition(BookCondition.GOOD)
            .createdAt(OffsetDateTime.now()).build();

    @Test
    void shouldCreateExchangeRequest() {
        when(userService.getEntity(1L)).thenReturn(requester);
        when(bookService.getEntity(10L)).thenReturn(requested);
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        var resp = service.create(1L, new ExchangeRequestCreateRequest(10L, null));

        assertThat(resp.status()).isEqualTo("WAITING");
    }

    @Test
    void shouldAcceptExchange() {
        ExchangeRequest er = ExchangeRequest.builder()
                .id(5L).requester(requester).owner(owner)
                .bookRequested(requested).status(ExchangeStatus.WAITING)
                .createdAt(OffsetDateTime.now()).build();

        when(repo.findById(5L)).thenReturn(Optional.of(er));

        var resp = service.accept(5L, 2L);

        assertThat(resp.status()).isEqualTo("ACCEPTED");
        assertThat(requested.getStatus()).isEqualTo(BookStatus.EXCHANGED);
    }

    @Test
    void shouldFailAcceptWhenWrongOwner() {
        ExchangeRequest er = ExchangeRequest.builder()
                .id(5L).requester(requester).owner(owner)
                .bookRequested(requested).status(ExchangeStatus.WAITING)
                .createdAt(OffsetDateTime.now()).build();

        when(repo.findById(5L)).thenReturn(Optional.of(er));

        assertThatThrownBy(() -> service.accept(5L, 99L))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldThrowWhenExchangeNotFound() {
        when(repo.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.accept(999L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Exchange not found");
    }

    @Test
    void shouldFailWhenOfferedBookDoesNotBelongToRequester() {
        Book offeredBook = Book.builder().id(20L).owner(owner).build();
        when(userService.getEntity(1L)).thenReturn(requester);
        when(bookService.getEntity(10L)).thenReturn(requested);
        when(bookService.getEntity(20L)).thenReturn(offeredBook);

        assertThatThrownBy(() -> service.create(1L, new ExchangeRequestCreateRequest(10L, 20L)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("does not belong to requester");
    }

    @Test
    void shouldAcceptExchangeWithOfferedBook() {
        Book offeredBook = Book.builder().id(20L).owner(requester)
                .status(BookStatus.AVAILABLE).build();
        
        ExchangeRequest er = ExchangeRequest.builder()
                .id(5L).requester(requester).owner(owner)
                .bookRequested(requested).bookOffered(offeredBook)
                .status(ExchangeStatus.WAITING)
                .createdAt(OffsetDateTime.now()).build();

        when(repo.findById(5L)).thenReturn(Optional.of(er));

        var resp = service.accept(5L, 2L);

        assertThat(resp.status()).isEqualTo("ACCEPTED");
        assertThat(requested.getStatus()).isEqualTo(BookStatus.EXCHANGED);
        assertThat(offeredBook.getStatus()).isEqualTo(BookStatus.EXCHANGED);
    }
}
