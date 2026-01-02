package dev.petr.exchange.controller;

import dev.petr.exchange.dto.ExchangeRequestCreateRequest;
import dev.petr.exchange.dto.ExchangeRequestResponse;
import dev.petr.exchange.service.ExchangeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/exchanges")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeService exchangeService;

    /**
     * Create a new exchange request
     * Only USER role can create exchanges
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExchangeRequestResponse create(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody ExchangeRequestCreateRequest request
    ) {
        if (role == null || !role.equals("ROLE_USER")) {
            throw new IllegalArgumentException("Only users can create exchanges");
        }

        log.info("User {} creating exchange request for book {}", userId, request.bookRequestedId());
        return exchangeService.create(userId, request);
    }

    /**
     * Accept an exchange request
     * Only the book owner (USER role) can accept
     */
    @PutMapping("/{id}/accept")
    public ExchangeRequestResponse accept(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long ownerId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        if (role == null || !role.equals("ROLE_USER")) {
            throw new IllegalArgumentException("Only users can accept exchanges");
        }

        log.info("User {} accepting exchange {}", ownerId, id);
        return exchangeService.accept(id, ownerId);
    }

    /**
     * Reject an exchange request (если есть такой метод)
     */
    @PutMapping("/{id}/reject")
    public ExchangeRequestResponse reject(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long ownerId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        if (role == null || !role.equals("ROLE_USER")) {
            throw new IllegalArgumentException("Only users can reject exchanges");
        }

        log.info("User {} rejecting exchange {}", ownerId, id);
        return exchangeService.reject(id, ownerId);
    }

    /**
     * Get paginated list of exchange requests
     * USER sees exchanges they're involved in
     * ADMIN sees all exchanges
     */
    @GetMapping
    public ResponseEntity<List<ExchangeRequestResponse>> page(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        Page<ExchangeRequestResponse> p;

        if (role != null && role.equals("ROLE_ADMIN")) {
            log.debug("Admin {} viewing all exchanges", userId);
            p = exchangeService.page(page, size);
        } else if (role != null && role.equals("ROLE_USER")) {
            log.debug("User {} viewing own exchanges", userId);
            p = exchangeService.page(page, size); // Или создайте метод pageForUser если нужна фильтрация
        } else {
            throw new IllegalArgumentException("Access denied");
        }

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(p.getTotalElements()))
                .body(p.getContent());
    }
}