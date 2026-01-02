package dev.petr.notification.application.dto;

import java.util.Map;

public record NotificationCreateRequest(
    Long userId,
    String type,
    String title,
    String message,
    Map<String, Object> data
) {}