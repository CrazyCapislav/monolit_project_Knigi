package dev.petr.notification.domain.model;

public enum NotificationType {
    EXCHANGE_REQUESTED,
    EXCHANGE_ACCEPTED,
    EXCHANGE_REJECTED,
    PUBLICATION_APPROVED,
    PUBLICATION_REJECTED,
    BOOK_AVAILABLE,
    FILE_UPLOADED,
    FILE_DELETED,
    SYSTEM_NOTIFICATION
}