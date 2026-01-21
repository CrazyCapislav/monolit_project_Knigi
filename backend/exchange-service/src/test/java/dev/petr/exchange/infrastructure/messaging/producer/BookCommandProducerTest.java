package dev.petr.exchange.infrastructure.messaging.producer;

import dev.petr.exchange.domain.event.BookCommandEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookCommandProducerTest {

    @Mock
    private KafkaTemplate<String, BookCommandEvent> bookCommandKafkaTemplate;

    @InjectMocks
    private BookCommandProducer producer;

    @Test
    void sendUpdateOwner_setsCommandTypeAndSends() {
        BookCommandEvent event = BookCommandEvent.builder()
                .bookId(12L)
                .currentOwnerId(1L)
                .newOwnerId(2L)
                .build();

        producer.sendUpdateOwner(event);

        assertEquals("UPDATE_OWNER", event.getCommandType());
        verify(bookCommandKafkaTemplate).send("book-command-events", "12", event);
    }
}
