package dev.petr.bookswap.controller;

import dev.petr.bookswap.dto.*;
import dev.petr.bookswap.service.PublicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.*;
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
@RequestMapping("/api/v1/publications")
@RequiredArgsConstructor
public class PublicationController {

    private final PublicationService service;

    @Operation(
            summary = "Создать заявку на публикацию",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = PublicationRequestCreateRequest.class))
            )
    )
    @PostMapping
    public ResponseEntity<PublicationRequestResponse> create(
            @Parameter(description = "ID заявителя")
            @RequestHeader("X-User-Id") Long requesterId,

            @RequestBody @Valid
            PublicationRequestCreateRequest req
    ) {
        PublicationRequestResponse r = service.create(requesterId, req);
        return ResponseEntity.created(URI.create("/api/v1/publications/" + r.id()))
                .body(r);
    }

    @Operation(summary = "Издатель одобряет заявку")
    @PostMapping("/{id}/approve")
    public PublicationRequestResponse approve(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long publisherId
    ) {
        return service.decide(id, publisherId, true);
    }

    @Operation(summary = "Издатель отклоняет заявку")
    @PostMapping("/{id}/reject")
    public PublicationRequestResponse reject(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long publisherId
    ) {
        return service.decide(id, publisherId, false);
    }

    @Operation(summary = "Постраничный список заявок на публикацию")
    @GetMapping
    public ResponseEntity<List<PublicationRequestResponse>> page(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        Page<PublicationRequestResponse> p = service.page(page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(p.getTotalElements()))
                .body(p.getContent());
    }
}
