package dev.petr.publication.infrastructure.messaging.producer;

import dev.petr.publication.domain.event.PublicationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublicationEventProducer {

    private final KafkaTemplate<String, PublicationEvent> kafkaTemplate;

    public void sendPublicationApproved(PublicationEvent event) {
        event.setEventType("PUBLICATION_APPROVED");
        log.info("Sending PUBLICATION_APPROVED event: {}", event);
        kafkaTemplate.send("book-events", event.getPublicationId().toString(), event);
    }

    public void sendPublicationRejected(PublicationEvent event) {
        event.setEventType("PUBLICATION_REJECTED");
        log.info("Sending PUBLICATION_REJECTED event: {}", event);
        kafkaTemplate.send("book-events", event.getPublicationId().toString(), event);
    }
}
