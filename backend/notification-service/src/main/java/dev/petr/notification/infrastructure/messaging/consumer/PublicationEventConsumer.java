package dev.petr.notification.infrastructure.messaging.consumer;

import dev.petr.notification.application.dto.NotificationCreateRequest;
import dev.petr.notification.application.usecase.CreateNotificationUseCase;
import dev.petr.notification.domain.event.PublicationEvent;
import dev.petr.notification.infrastructure.messaging.producer.NotificationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublicationEventConsumer {

    private final CreateNotificationUseCase createNotificationUseCase;
    private final NotificationProducer notificationProducer;

    @KafkaListener(
            topics = "book-events",
            groupId = "notification-service",
            containerFactory = "publicationEventKafkaListenerContainerFactory"
    )
    public void consume(PublicationEvent event) {
        log.info("Received publication event: {}", event);

        try {
            switch (event.getEventType()) {
                case "PUBLICATION_APPROVED" -> handlePublicationApproved(event);
                case "PUBLICATION_REJECTED" -> handlePublicationRejected(event);
                default -> log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing publication event", e);
        }
    }

    private void handlePublicationApproved(PublicationEvent event) {
        NotificationCreateRequest request = new NotificationCreateRequest(
                event.getUserId(),
                "PUBLICATION_APPROVED",
                "Book Publication Approved",
                "Your book has been approved and is now available!",
                Map.of(
                        "publicationId", event.getPublicationId(),
                        "bookId", event.getBookId()
                )
        );

        var notification = createNotificationUseCase.execute(request);
        notificationProducer.sendNotification(notification);
    }

    private void handlePublicationRejected(PublicationEvent event) {
        NotificationCreateRequest request = new NotificationCreateRequest(
                event.getUserId(),
                "PUBLICATION_REJECTED",
                "Book Publication Rejected",
                "Your book publication request has been rejected",
                Map.of(
                        "publicationId", event.getPublicationId()
                )
        );

        var notification = createNotificationUseCase.execute(request);
        notificationProducer.sendNotification(notification);
    }
}