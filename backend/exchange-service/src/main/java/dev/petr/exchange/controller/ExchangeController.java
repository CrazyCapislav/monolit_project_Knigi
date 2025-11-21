package dev.petr.exchange.controller;

import dev.petr.exchange.dto.ExchangeRequestCreateRequest;
import dev.petr.exchange.dto.ExchangeRequestResponse;
import dev.petr.exchange.service.ExchangeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exchanges")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeService exchangeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExchangeRequestResponse create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ExchangeRequestCreateRequest request
    ) {
        return exchangeService.create(userId, request);
    }

    @PutMapping("/{id}/accept")
    public ExchangeRequestResponse accept(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long ownerId
    ) {
        return exchangeService.accept(id, ownerId);
    }

    @GetMapping
    public ResponseEntity<List<ExchangeRequestResponse>> page(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        Page<ExchangeRequestResponse> p = exchangeService.page(page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(p.getTotalElements()))
                .body(p.getContent());
    }
}
