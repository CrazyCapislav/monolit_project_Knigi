package dev.petr.notification.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
public class Notification {
    private Long id;
    private Long userId;
    private NotificationType type;
    private String title;
    private String message;
    private Map<String, Object> data;
    private boolean read;
    private OffsetDateTime createdAt;
    private OffsetDateTime readAt;
}