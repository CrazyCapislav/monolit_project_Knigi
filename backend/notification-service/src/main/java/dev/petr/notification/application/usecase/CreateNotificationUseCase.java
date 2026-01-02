package dev.petr.notification.application.usecase;

import dev.petr.notification.application.dto.NotificationCreateRequest;
import dev.petr.notification.application.dto.NotificationResponse;
import dev.petr.notification.domain.model.Notification;
import dev.petr.notification.domain.model.NotificationType;
import dev.petr.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateNotificationUseCase {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationResponse execute(NotificationCreateRequest request) {
        log.info("Creating notification for user {}", request.userId());

        Notification notification = Notification.builder()
                .userId(request.userId())
                .type(NotificationType.valueOf(request.type()))
                .title(request.title())
                .message(request.message())
                .data(request.data())
                .read(false)
                .createdAt(OffsetDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification {} created successfully", saved.getId());

        return toResponse(saved);
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