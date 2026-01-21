package dev.petr.exchange.infrastructure.messaging.producer;

import dev.petr.exchange.domain.event.BookCommandEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookCommandProducer {

    private final KafkaTemplate<String, BookCommandEvent> bookCommandKafkaTemplate;

    public void sendUpdateOwner(BookCommandEvent event) {
        event.setCommandType("UPDATE_OWNER");
        log.info("Sending UPDATE_OWNER command: {}", event);
        bookCommandKafkaTemplate.send("book-command-events", event.getBookId().toString(), event);
    }
}
