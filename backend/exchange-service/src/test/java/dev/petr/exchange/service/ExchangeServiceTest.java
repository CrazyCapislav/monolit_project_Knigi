package dev.petr.exchange.service;

import dev.petr.exchange.client.BookServiceClient;
import dev.petr.exchange.dto.BookResponse;
import dev.petr.exchange.dto.ExchangeRequestCreateRequest;
import dev.petr.exchange.dto.UpdateBookOwnerRequest;
import dev.petr.exchange.entity.ExchangeRequest;
import dev.petr.exchange.entity.ExchangeStatus;
import dev.petr.exchange.repository.ExchangeRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeServiceTest {

    @Mock
    private ExchangeRequestRepository exchangeRepository;

    @Mock
    private BookServiceClient bookServiceClient;

    @InjectMocks
    private ExchangeService exchangeService;

    private ExchangeRequest testExchange;
    private BookResponse requestedBook;
    private BookResponse offeredBook;

    @BeforeEach
    void setUp() {
        requestedBook = new BookResponse(
                1L, "Book 1", "Author 1", null, null,
                "AVAILABLE", "GOOD", null, 2L, null
        );

        offeredBook = new BookResponse(
                2L, "Book 2", "Author 2", null, null,
                "AVAILABLE", "NEW", null, 1L, null
        );

        testExchange = ExchangeRequest.builder()
                .id(1L)
                .requesterId(1L)
                .ownerId(2L)
                .bookRequestedId(1L)
                .bookOfferedId(2L)
                .status(ExchangeStatus.WAITING)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void create_Success() {
        
        ExchangeRequestCreateRequest request = new ExchangeRequestCreateRequest(1L, 2L);

        when(bookServiceClient.getBook(1L, 1L)).thenReturn(requestedBook);
        when(bookServiceClient.getBook(2L, 1L)).thenReturn(offeredBook);
        when(exchangeRepository.save(any(ExchangeRequest.class))).thenReturn(testExchange);

        
        var response = exchangeService.create(1L, request);

        
        assertNotNull(response);
        assertEquals(ExchangeStatus.WAITING.name(), response.status());
        verify(exchangeRepository).save(any(ExchangeRequest.class));
    }

    @Test
    void create_RequestOwnBook_ThrowsException() {
        
        ExchangeRequestCreateRequest request = new ExchangeRequestCreateRequest(1L, null);
        BookResponse ownBook = new BookResponse(
                1L, "My Book", "Me", null, null,
                "AVAILABLE", "GOOD", null, 1L, null
        );

        when(bookServiceClient.getBook(1L, 1L)).thenReturn(ownBook);

        assertThrows(IllegalArgumentException.class,
                () -> exchangeService.create(1L, request));
        verify(exchangeRepository, never()).save(any());
    }

    @Test
    void create_BookNotAvailable_ThrowsException() {
        
        ExchangeRequestCreateRequest request = new ExchangeRequestCreateRequest(1L, null);
        BookResponse unavailableBook = new BookResponse(
                1L, "Book", "Author", null, null,
                "EXCHANGED", "GOOD", null, 2L, null
        );

        when(bookServiceClient.getBook(1L, 1L)).thenReturn(unavailableBook);

        assertThrows(IllegalArgumentException.class,
                () -> exchangeService.create(1L, request));
    }

    @Test
    void create_OfferNotOwnBook_ThrowsException() {
        
        ExchangeRequestCreateRequest request = new ExchangeRequestCreateRequest(1L, 2L);
        BookResponse notMyBook = new BookResponse(
                2L, "Not Mine", "Author", null, null,
                "AVAILABLE", "GOOD", null, 999L, null
        );

        when(bookServiceClient.getBook(1L, 1L)).thenReturn(requestedBook);
        when(bookServiceClient.getBook(2L, 1L)).thenReturn(notMyBook);

        assertThrows(IllegalArgumentException.class,
                () -> exchangeService.create(1L, request));
    }

    @Test
    void accept_Success_SwapsOwnership() {
        
        when(exchangeRepository.findById(1L)).thenReturn(Optional.of(testExchange));
        when(bookServiceClient.updateOwner(anyLong(), any(UpdateBookOwnerRequest.class), anyLong()))
                .thenReturn(requestedBook);
        when(exchangeRepository.save(any(ExchangeRequest.class))).thenReturn(testExchange);

        
        var response = exchangeService.accept(1L, 2L);

        
        assertNotNull(response);
        assertEquals(ExchangeStatus.ACCEPTED.name(), response.status());

        verify(bookServiceClient).updateOwner(eq(1L), any(UpdateBookOwnerRequest.class), eq(2L));
        verify(bookServiceClient).updateOwner(eq(2L), any(UpdateBookOwnerRequest.class), eq(1L));
        verify(exchangeRepository).save(any(ExchangeRequest.class));
    }

    @Test
    void accept_NotOwner_ThrowsException() {
        
        when(exchangeRepository.findById(1L)).thenReturn(Optional.of(testExchange));

        assertThrows(IllegalArgumentException.class,
                () -> exchangeService.accept(1L, 999L));

        verify(bookServiceClient, never()).updateOwner(anyLong(), any(), anyLong());
        verify(exchangeRepository, never()).save(any());
    }

    @Test
    void accept_AlreadyAccepted_ThrowsException() {
        
        testExchange.setStatus(ExchangeStatus.ACCEPTED);
        when(exchangeRepository.findById(1L)).thenReturn(Optional.of(testExchange));

        assertThrows(IllegalArgumentException.class,
                () -> exchangeService.accept(1L, 2L));

        verify(bookServiceClient, never()).updateOwner(anyLong(), any(), anyLong());
    }

    @Test
    void accept_ExchangeNotFound_ThrowsException() {
        
        when(exchangeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> exchangeService.accept(999L, 2L));
    }
}