package dev.petr.publication.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDomainEvent {
    private String eventType;
    private Long bookId;
    private Long ownerId;
    private Long publicationRequestId;
}
