package dev.petr.publication.client;

import dev.petr.publication.dto.BookResponse;
import dev.petr.publication.dto.CreateBookRequest;
import dev.petr.publication.fallback.BookServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "book-service",
        fallback = BookServiceFallback.class
)
public interface BookServiceClient {

    @PostMapping("/api/v1/books")
    BookResponse createBook(
            @RequestBody CreateBookRequest request,
            @RequestHeader("X-User-Id") Long publisherId
    );
}