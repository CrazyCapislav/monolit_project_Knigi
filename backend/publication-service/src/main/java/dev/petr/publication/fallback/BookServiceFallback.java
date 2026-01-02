package dev.petr.publication.fallback;

import dev.petr.publication.client.BookServiceClient;
import dev.petr.publication.dto.BookResponse;
import dev.petr.publication.dto.CreateBookRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BookServiceFallback implements BookServiceClient {

    @Override
    public BookResponse createBook(CreateBookRequest request, Long publisherId) {
        log.warn("Circuit breaker activated! Cannot create book in Book Service");
        throw new RuntimeException("Book service is temporarily unavailable");
    }
}