package dev.petr.book.infrastructure.messaging.producer;

import dev.petr.book.domain.event.BookDomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookEventProducer {

    private final KafkaTemplate<String, BookDomainEvent> kafkaTemplate;

    public void sendBookCreated(BookDomainEvent event) {
        event.setEventType("BOOK_CREATED");
        log.info("Sending BOOK_CREATED event: {}", event);
        kafkaTemplate.send("book-domain-events", event.getBookId().toString(), event);
    }

    public void sendOwnerUpdated(BookDomainEvent event) {
        event.setEventType("BOOK_OWNER_UPDATED");
        log.info("Sending BOOK_OWNER_UPDATED event: {}", event);
        kafkaTemplate.send("book-domain-events", event.getBookId().toString(), event);
    }
}
