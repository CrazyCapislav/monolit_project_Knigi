package dev.petr.notification.application.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record NotificationResponse(
    Long id,
    Long userId,
    String type,
    String title,
    String message,
    Map<String, Object> data,
    boolean read,
    OffsetDateTime createdAt,
    OffsetDateTime readAt
) {}