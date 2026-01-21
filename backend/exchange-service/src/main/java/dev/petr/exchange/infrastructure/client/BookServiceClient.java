package dev.petr.exchange.infrastructure.client;

import dev.petr.exchange.application.dto.BookResponse;
import dev.petr.exchange.infrastructure.fallback.BookServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "book-service",
        fallback = BookServiceFallback.class
)
public interface BookServiceClient {
    
    @GetMapping("/api/v1/books/{id}")
    BookResponse getBook(
            @PathVariable("id") Long id,
            @RequestHeader("X-User-Id") Long userId
    );

}

