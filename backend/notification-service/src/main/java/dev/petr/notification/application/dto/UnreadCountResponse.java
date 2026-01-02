package dev.petr.notification.application.dto;

public record UnreadCountResponse(
    Long userId,
    long count
) {}