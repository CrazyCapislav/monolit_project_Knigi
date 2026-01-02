package dev.petr.notification.domain.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookEvent {
    private Long bookId;
    private Long ownerId;
    private String title;
    private String status;
    private String eventType;
}