package dev.petr.notification.infrastructure.messaging.consumer;

import dev.petr.notification.application.dto.NotificationCreateRequest;
import dev.petr.notification.application.usecase.CreateNotificationUseCase;
import dev.petr.notification.domain.event.ExchangeEvent;
import dev.petr.notification.infrastructure.messaging.producer.NotificationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeEventConsumer {

    private final CreateNotificationUseCase createNotificationUseCase;
    private final NotificationProducer notificationProducer;

    @KafkaListener(
            topics = "exchange-events",
            groupId = "notification-service",
            containerFactory = "exchangeEventKafkaListenerContainerFactory"
    )
    public void consume(ExchangeEvent event) {
        log.info("Received exchange event: {}", event);

        try {
            switch (event.getEventType()) {
                case "EXCHANGE_REQUESTED" -> handleExchangeRequested(event);
                case "EXCHANGE_ACCEPTED" -> handleExchangeAccepted(event);
                case "EXCHANGE_REJECTED" -> handleExchangeRejected(event);
                default -> log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing exchange event", e);
        }
    }

    private void handleExchangeRequested(ExchangeEvent event) {
        NotificationCreateRequest request = new NotificationCreateRequest(
                event.getOwnerId(),
                "EXCHANGE_REQUESTED",
                "New Exchange Request",
                String.format("User has requested to exchange books with you"),
                Map.of(
                        "exchangeId", event.getExchangeId(),
                        "requesterId", event.getRequesterId(),
                        "bookRequestedId", event.getBookRequestedId(),
                        "bookOfferedId", event.getBookOfferedId() != null ? event.getBookOfferedId() : 0
                )
        );

        var notification = createNotificationUseCase.execute(request);
        notificationProducer.sendNotification(notification);
    }

    private void handleExchangeAccepted(ExchangeEvent event) {
        NotificationCreateRequest request = new NotificationCreateRequest(
                event.getRequesterId(),
                "EXCHANGE_ACCEPTED",
                "Exchange Request Accepted",
                "Your exchange request has been accepted!",
                Map.of(
                        "exchangeId", event.getExchangeId(),
                        "ownerId", event.getOwnerId(),
                        "bookRequestedId", event.getBookRequestedId()
                )
        );

        var notification = createNotificationUseCase.execute(request);
        notificationProducer.sendNotification(notification);
    }

    private void handleExchangeRejected(ExchangeEvent event) {
        NotificationCreateRequest request = new NotificationCreateRequest(
                event.getRequesterId(),
                "EXCHANGE_REJECTED",
                "Exchange Request Rejected",
                "Your exchange request has been rejected",
                Map.of(
                        "exchangeId", event.getExchangeId(),
                        "bookRequestedId", event.getBookRequestedId()
                )
        );

        var notification = createNotificationUseCase.execute(request);
        notificationProducer.sendNotification(notification);
    }
}