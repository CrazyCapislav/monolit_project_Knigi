package dev.petr.exchange.controller;

import dev.petr.exchange.dto.ExchangeRequestCreateRequest;
import dev.petr.exchange.dto.ExchangeRequestResponse;
import dev.petr.exchange.service.ExchangeService;
import io.swagger.v3.oas.annotations.Parameter;
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


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExchangeRequestResponse create(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody ExchangeRequestCreateRequest request
    ) {
        if (role == null || !role.equals("ROLE_USER")) {
            throw new IllegalArgumentException("Only users can create exchanges");
        }

        log.info("User {} creating exchange request for book {}", userId, request.bookRequestedId());
        return exchangeService.create(userId, request);
    }


    @PutMapping("/{id}/accept")
    public ExchangeRequestResponse accept(
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long ownerId,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        if (role == null || !role.equals("ROLE_USER")) {
            throw new IllegalArgumentException("Only users can accept exchanges");
        }

        log.info("User {} accepting exchange {}", ownerId, id);
        return exchangeService.accept(id, ownerId);
    }

    @PutMapping("/{id}/reject")
    public ExchangeRequestResponse reject(
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long ownerId,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        if (role == null || !role.equals("ROLE_USER")) {
            throw new IllegalArgumentException("Only users can reject exchanges");
        }

        log.info("User {} rejecting exchange {}", ownerId, id);
        return exchangeService.reject(id, ownerId);
    }


    @GetMapping
    public ResponseEntity<List<ExchangeRequestResponse>> page(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long userId,
            @Parameter(hidden = true) @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        Page<ExchangeRequestResponse> p;

        if (role != null && role.equals("ROLE_ADMIN")) {
            log.debug("Admin {} viewing all exchanges", userId);
            p = exchangeService.page(page, size);
        } else if (role != null && role.equals("ROLE_USER")) {
            log.debug("User {} viewing own exchanges", userId);
            p = exchangeService.page(page, size); 
        } else {
            throw new IllegalArgumentException("Access denied");
        }

        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(p.getTotalElements()))
                .body(p.getContent());
    }

//    /**
//     * Cancel exchange request by admin
//     * Only ADMIN can cancel any exchange request
//     */
//    @PutMapping("/{id}/cancel")
//    public ExchangeRequestResponse cancel(
//            @PathVariable Long id,
//            @Parameter(hidden = true) @RequestHeader("X-User-Id") Long adminId,
//            @Parameter(hidden = true) @RequestHeader(value = "X-User-Role", required = false) String role
//    ) {
//        if (role == null || !role.equals("ROLE_ADMIN")) {
//            throw new IllegalArgumentException("Only admins can cancel exchange requests");
//        }
//
//        log.info("Admin {} canceling exchange {}", adminId, id);
//        return exchangeService.cancelByAdmin(id);
//    }
}