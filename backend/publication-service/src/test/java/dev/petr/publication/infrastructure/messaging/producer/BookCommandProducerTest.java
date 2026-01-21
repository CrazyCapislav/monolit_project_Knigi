package dev.petr.publication.infrastructure.messaging.producer;

import dev.petr.publication.domain.event.BookCommandEvent;
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
    void sendCreateBook_setsCommandTypeAndSends() {
        BookCommandEvent event = BookCommandEvent.builder()
                .publicationRequestId(20L)
                .title("Book")
                .build();

        producer.sendCreateBook(event);

        assertEquals("CREATE_BOOK", event.getCommandType());
        verify(bookCommandKafkaTemplate).send("book-command-events", "20", event);
    }
}
