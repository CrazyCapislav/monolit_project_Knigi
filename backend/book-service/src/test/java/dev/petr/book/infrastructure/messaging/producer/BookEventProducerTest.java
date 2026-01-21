package dev.petr.book.infrastructure.messaging.producer;

import dev.petr.book.domain.event.BookDomainEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookEventProducerTest {

    @Mock
    private KafkaTemplate<String, BookDomainEvent> kafkaTemplate;

    @InjectMocks
    private BookEventProducer producer;

    @Test
    void sendBookCreated_setsTypeAndSends() {
        BookDomainEvent event = BookDomainEvent.builder()
                .bookId(10L)
                .ownerId(5L)
                .build();

        producer.sendBookCreated(event);

        assertEquals("BOOK_CREATED", event.getEventType());
        verify(kafkaTemplate).send("book-domain-events", "10", event);
    }

    @Test
    void sendOwnerUpdated_setsTypeAndSends() {
        BookDomainEvent event = BookDomainEvent.builder()
                .bookId(7L)
                .ownerId(3L)
                .build();

        producer.sendOwnerUpdated(event);

        assertEquals("BOOK_OWNER_UPDATED", event.getEventType());
        verify(kafkaTemplate).send("book-domain-events", "7", event);
    }
}
