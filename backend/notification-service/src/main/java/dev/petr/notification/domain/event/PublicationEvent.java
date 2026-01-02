package dev.petr.notification.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicationEvent {
    private Long publicationId;
    private Long userId;
    private Long bookId;
    private String status;
    private String eventType;
}