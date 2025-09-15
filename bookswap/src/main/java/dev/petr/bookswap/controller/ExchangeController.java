package dev.petr.bookswap.controller;

import dev.petr.bookswap.dto.*;
import dev.petr.bookswap.service.ExchangeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController @RequestMapping("/api/v1/exchanges")
public class ExchangeController {
    private final ExchangeService service;
    public ExchangeController(ExchangeService service){ this.service=service; }

    @PostMapping
    public ResponseEntity<ExchangeRequestResponse> create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ExchangeRequestCreateRequest req){
        ExchangeRequestResponse r = service.create(userId,req);
        return ResponseEntity.created(URI.create("/api/v1/exchanges/"+r.id())).body(r);
    }

    @PostMapping("/{id}/accept")
    public ExchangeRequestResponse accept(@PathVariable Long id,
                                          @RequestHeader("X-User-Id") Long ownerId){
        return service.accept(id, ownerId);
    }

    @GetMapping
    public ResponseEntity<List<ExchangeRequestResponse>> page(
            @RequestParam(defaultValue="0") @Min(0) int page,
            @RequestParam(defaultValue="20") @Min(1) @Max(50) int size){
        Page<ExchangeRequestResponse> p = service.page(page,size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(p.getTotalElements()))
                .body(p.getContent());
    }
}
