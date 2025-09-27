package dev.petr.bookswap.controller;

import dev.petr.bookswap.dto.*;
import dev.petr.bookswap.service.ExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/exchanges")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeService service;

    @Operation(
            summary = "Создать заявку на обмен",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = ExchangeRequestCreateRequest.class))
            )
    )
    @PostMapping
    public ResponseEntity<ExchangeRequestResponse> create(
            @Parameter(description = "ID пользователя-заявителя")
            @RequestHeader("X-User-Id") Long userId,

            @RequestBody @Valid
            ExchangeRequestCreateRequest req
    ) {
        ExchangeRequestResponse r = service.create(userId, req);
        return ResponseEntity.created(URI.create("/api/v1/exchanges/" + r.id()))
                .body(r);
    }

    @Operation(summary = "Владелец принимает обмен")
    @PostMapping("/{id}/accept")
    public ExchangeRequestResponse accept(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long ownerId
    ) {
        return service.accept(id, ownerId);
    }

    @Operation(summary = "Постраничный список заявок на обмен")
    @GetMapping
    public ResponseEntity<List<ExchangeRequestResponse>> page(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        Page<ExchangeRequestResponse> p = service.page(page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(p.getTotalElements()))
                .body(p.getContent());
    }
}
