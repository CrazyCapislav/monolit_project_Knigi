package dev.petr.notification.infrastructure.messaging.producer;

import dev.petr.notification.application.dto.NotificationResponse;
import dev.petr.notification.infrastructure.websocket.WebSocketNotificationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<String, NotificationResponse> kafkaTemplate;
    private final WebSocketNotificationHandler webSocketHandler;

    public void sendNotification(NotificationResponse notification) {
        log.info("Sending notification {} to user {}", notification.id(), notification.userId());
        
        kafkaTemplate.send("notification-events", 
                notification.userId().toString(), 
                notification);
        
        webSocketHandler.sendToUser(notification.userId(), notification);
    }
}