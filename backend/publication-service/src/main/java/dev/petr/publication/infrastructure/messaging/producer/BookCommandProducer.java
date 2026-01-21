package dev.petr.publication.infrastructure.messaging.producer;

import dev.petr.publication.domain.event.BookCommandEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookCommandProducer {

    private final KafkaTemplate<String, BookCommandEvent> bookCommandKafkaTemplate;

    public void sendCreateBook(BookCommandEvent event) {
        event.setCommandType("CREATE_BOOK");
        log.info("Sending CREATE_BOOK command: {}", event);
        bookCommandKafkaTemplate.send("book-command-events", event.getPublicationRequestId().toString(), event);
    }
}
