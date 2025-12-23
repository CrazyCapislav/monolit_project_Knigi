package dev.petr.exchange.fallback;

import dev.petr.exchange.client.BookServiceClient;
import dev.petr.exchange.dto.BookResponse;
import dev.petr.exchange.dto.UpdateBookOwnerRequest;
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

    @Override
    public BookResponse updateOwner(Long bookId, UpdateBookOwnerRequest request, Long userId) {
        log.warn("Circuit breaker activated! Cannot update book owner. Book Service unavailable.");
        throw new RuntimeException("Book service is temporarily unavailable");
    }
}
