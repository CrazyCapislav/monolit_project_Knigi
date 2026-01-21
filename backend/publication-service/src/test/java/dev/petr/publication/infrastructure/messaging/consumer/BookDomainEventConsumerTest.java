package dev.petr.publication.infrastructure.messaging.consumer;

import dev.petr.publication.domain.event.BookDomainEvent;
import dev.petr.publication.domain.model.PublicationRequest;
import dev.petr.publication.domain.model.PublicationStatus;
import dev.petr.publication.infrastructure.persistence.repository.PublicationRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookDomainEventConsumerTest {

    @Mock
    private PublicationRequestRepository publicationRepository;

    @InjectMocks
    private BookDomainEventConsumer consumer;

    @Test
    void consumeBookCreated_updatesPublicationRequest() {
        PublicationRequest request = PublicationRequest.builder()
                .id(1L)
                .requesterId(5L)
                .title("Title")
                .author("Author")
                .status(PublicationStatus.PUBLISHING)
                .createdAt(OffsetDateTime.now())
                .build();

        when(publicationRepository.findById(1L)).thenReturn(Optional.of(request));

        BookDomainEvent event = BookDomainEvent.builder()
                .eventType("BOOK_CREATED")
                .bookId(10L)
                .publicationRequestId(1L)
                .build();

        consumer.consume(event);

        ArgumentCaptor<PublicationRequest> captor = ArgumentCaptor.forClass(PublicationRequest.class);
        verify(publicationRepository).save(captor.capture());
        PublicationRequest saved = captor.getValue();
        assertEquals(PublicationStatus.PUBLISHED, saved.getStatus());
        assertEquals(10L, saved.getCreatedBookId());
    }

    @Test
    void consumeNonBookCreated_ignoresEvent() {
        BookDomainEvent event = BookDomainEvent.builder()
                .eventType("OTHER")
                .publicationRequestId(1L)
                .build();

        consumer.consume(event);

        verifyNoInteractions(publicationRepository);
    }

    @Test
    void consumeMissingPublicationRequestId_ignoresEvent() {
        BookDomainEvent event = BookDomainEvent.builder()
                .eventType("BOOK_CREATED")
                .bookId(10L)
                .build();

        consumer.consume(event);

        verifyNoInteractions(publicationRepository);
    }
}
