package dev.petr.publication.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicationEvent {
    private Long publicationId;
    private Long userId;
    private Long bookId;
    private String status;
    private String eventType;
}