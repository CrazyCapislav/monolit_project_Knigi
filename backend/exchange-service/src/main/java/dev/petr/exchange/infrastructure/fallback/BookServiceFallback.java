package dev.petr.exchange.infrastructure.fallback;

import dev.petr.exchange.infrastructure.client.BookServiceClient;
import dev.petr.exchange.application.dto.BookResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BookServiceFallback implements BookServiceClient {
    
    @Override
    public BookResponse getBook(Long id, Long userId) {
        log.warn("Circuit breaker activated! Book Service is unavailable. Returning fallback for book {}", id);

        return new BookResponse(
                id,
                "Book temporarily unavailable",
                "Unknown",
                null,
                null,
                "UNKNOWN",
                "UNKNOWN",
                null,
                null,
                null
        );
    }

}

