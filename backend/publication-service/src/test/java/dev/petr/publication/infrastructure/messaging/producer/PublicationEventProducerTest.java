package dev.petr.publication.infrastructure.messaging.producer;

import dev.petr.publication.domain.event.PublicationEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PublicationEventProducerTest {

    @Mock
    private KafkaTemplate<String, PublicationEvent> kafkaTemplate;

    @InjectMocks
    private PublicationEventProducer producer;

    @Test
    void sendPublicationApproved_setsTypeAndSends() {
        PublicationEvent event = PublicationEvent.builder().publicationId(1L).build();
        producer.sendPublicationApproved(event);
        assertEquals("PUBLICATION_APPROVED", event.getEventType());
        verify(kafkaTemplate).send("book-events", "1", event);
    }

    @Test
    void sendPublicationRejected_setsTypeAndSends() {
        PublicationEvent event = PublicationEvent.builder().publicationId(2L).build();
        producer.sendPublicationRejected(event);
        assertEquals("PUBLICATION_REJECTED", event.getEventType());
        verify(kafkaTemplate).send("book-events", "2", event);
    }
}
