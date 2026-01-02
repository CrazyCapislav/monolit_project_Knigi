package dev.petr.exchange.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeEvent {
    private Long exchangeId;
    private Long requesterId;
    private Long ownerId;
    private Long bookOfferedId;
    private Long bookRequestedId;
    private String status;
    private String eventType;
}