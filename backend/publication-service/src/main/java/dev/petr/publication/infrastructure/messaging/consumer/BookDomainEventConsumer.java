package dev.petr.publication.infrastructure.messaging.consumer;

import dev.petr.publication.domain.event.BookDomainEvent;
import dev.petr.publication.domain.model.PublicationRequest;
import dev.petr.publication.domain.model.PublicationStatus;
import dev.petr.publication.infrastructure.persistence.repository.PublicationRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookDomainEventConsumer {

    private final PublicationRequestRepository publicationRepository;

    @KafkaListener(
            topics = "book-domain-events",
            groupId = "publication-service",
            containerFactory = "bookDomainEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void consume(BookDomainEvent event) {
        if (event == null || !"BOOK_CREATED".equals(event.getEventType())) {
            return;
        }

        if (event.getPublicationRequestId() == null) {
            log.warn("Book created event without publication request id: {}", event);
            return;
        }

        PublicationRequest request = publicationRepository.findById(event.getPublicationRequestId())
                .orElse(null);
        if (request == null) {
            log.warn("Publication request {} not found for book {}", event.getPublicationRequestId(), event.getBookId());
            return;
        }

        request.setCreatedBookId(event.getBookId());
        request.setStatus(PublicationStatus.PUBLISHED);
        publicationRepository.save(request);
        log.info("Publication request {} updated with book {}", request.getId(), event.getBookId());
    }
}
