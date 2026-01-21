package dev.petr.exchange.client;

import dev.petr.exchange.dto.BookResponse;
import dev.petr.exchange.dto.UpdateBookOwnerRequest;
import dev.petr.exchange.fallback.BookServiceFallback;
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

    @PutMapping("/api/v1/books/{id}/owner")
    BookResponse updateOwner(
            @PathVariable("id") Long bookId,
            @RequestBody UpdateBookOwnerRequest request,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String userRole
    );
}
