package dev.petr.notification.infrastructure.messaging.consumer;

import dev.petr.notification.application.dto.NotificationResponse;
import dev.petr.notification.domain.event.FileEvent;
import dev.petr.notification.domain.model.Notification;
import dev.petr.notification.domain.model.NotificationType;
import dev.petr.notification.domain.repository.NotificationRepository;
import dev.petr.notification.infrastructure.websocket.WebSocketNotificationHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileEventConsumer {

    private final NotificationRepository notificationRepository;
    private final WebSocketNotificationHandler webSocketHandler;

    @KafkaListener(
            topics = "file-events",
            groupId = "notification-service",
            containerFactory = "fileEventKafkaListenerContainerFactory"
    )
    public void consume(FileEvent event) {
        log.info("Received file event: {}", event);

        try {
            if ("FILE_UPLOADED".equals(event.getEventType())) {
                handleFileUploaded(event);
            }
        } catch (Exception e) {
            log.error("Error processing file event", e);
        }
    }

    private void handleFileUploaded(FileEvent event) {
        Map<String, Object> data = new HashMap<>();
        data.put("fileId", event.getFileId());
        data.put("fileName", event.getFileName());
        data.put("fileType", event.getFileType());
        data.put("entityType", event.getEntityType());
        data.put("entityId", event.getEntityId());

        Notification notification = Notification.builder()
                .userId(event.getUploaderId())
                .type(NotificationType.FILE_UPLOADED)
                .title("File Uploaded")
                .message("Your file " + event.getFileName() + " has been uploaded successfully")
                .data(data)
                .read(false)
                .createdAt(OffsetDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("File upload notification created: {}", saved.getId());

        NotificationResponse response = toResponse(saved);
        webSocketHandler.sendToUser(event.getUploaderId(), response);
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getType().name(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getData(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}