package dev.petr.book.infrastructure.messaging.consumer;

import dev.petr.book.application.dto.BookCreateRequest;
import dev.petr.book.application.dto.BookResponse;
import dev.petr.book.application.service.BookService;
import dev.petr.book.domain.event.BookCommandEvent;
import dev.petr.book.domain.event.BookDomainEvent;
import dev.petr.book.infrastructure.messaging.producer.BookEventProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookCommandConsumerTest {

    @Mock
    private BookService bookService;

    @Mock
    private BookEventProducer bookEventProducer;

    @InjectMocks
    private BookCommandConsumer consumer;

    @Test
    void consumeCreateCommand_sendsBookCreatedEvent() {
        BookCommandEvent command = BookCommandEvent.builder()
                .commandType("CREATE_BOOK")
                .ownerId(1L)
                .publicationRequestId(99L)
                .title("Title")
                .author("Author")
                .isbn("123")
                .publishedYear(2024)
                .condition("NEW")
                .genreIds(Set.of(1L))
                .build();

        BookResponse created = new BookResponse(
                10L,
                "Title",
                "Author",
                "123",
                2024,
                "AVAILABLE",
                "NEW",
                OffsetDateTime.now(),
                1L,
                Set.of("Genre"),
                null,
                null
        );

        when(bookService.create(eq(1L), any(BookCreateRequest.class))).thenReturn(Mono.just(created));

        consumer.consume(command);

        ArgumentCaptor<BookDomainEvent> captor = ArgumentCaptor.forClass(BookDomainEvent.class);
        verify(bookEventProducer).sendBookCreated(captor.capture());
        BookDomainEvent event = captor.getValue();
        assertEquals(10L, event.getBookId());
        assertEquals(1L, event.getOwnerId());
        assertEquals(99L, event.getPublicationRequestId());
    }

    @Test
    void consumeUpdateOwnerCommand_sendsOwnerUpdatedEvent() {
        BookCommandEvent command = BookCommandEvent.builder()
                .commandType("UPDATE_OWNER")
                .bookId(5L)
                .currentOwnerId(2L)
                .newOwnerId(3L)
                .build();

        BookResponse updated = new BookResponse(
                5L,
                "Title",
                "Author",
                "123",
                2024,
                "AVAILABLE",
                "NEW",
                OffsetDateTime.now(),
                3L,
                Set.of("Genre"),
                null,
                null
        );

        when(bookService.updateOwner(5L, 2L, 3L)).thenReturn(Mono.just(updated));

        consumer.consume(command);

        ArgumentCaptor<BookDomainEvent> captor = ArgumentCaptor.forClass(BookDomainEvent.class);
        verify(bookEventProducer).sendOwnerUpdated(captor.capture());
        BookDomainEvent event = captor.getValue();
        assertEquals(5L, event.getBookId());
        assertEquals(3L, event.getOwnerId());
    }

    @Test
    void consumeUnknownCommand_noActions() {
        BookCommandEvent command = BookCommandEvent.builder()
                .commandType("UNKNOWN")
                .build();

        consumer.consume(command);

        verifyNoInteractions(bookService, bookEventProducer);
    }

    @Test
    void consumeNullCommand_noActions() {
        consumer.consume(null);

        verifyNoInteractions(bookService, bookEventProducer);
    }
}
