package dev.petr.exchange.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookCommandEvent {
    private String commandType;
    private Long bookId;
    private Long currentOwnerId;
    private Long newOwnerId;
}
