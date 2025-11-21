package dev.petr.publication.controller;

import dev.petr.publication.dto.PublicationRequestCreateRequest;
import dev.petr.publication.dto.PublicationRequestResponse;
import dev.petr.publication.service.PublicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/publications")
@RequiredArgsConstructor
public class PublicationController {

    private final PublicationService publicationService;

    /**
     * User requests a book that doesn't exist
     */
    @PostMapping("/request")
    @ResponseStatus(HttpStatus.CREATED)
    public PublicationRequestResponse requestBook(
            @RequestHeader("X-User-Id") Long requesterId,
            @Valid @RequestBody PublicationRequestCreateRequest request
    ) {
        return publicationService.requestBook(requesterId, request);
    }

    /**
     * Publisher approves the request
     * Requires ROLE_PUBLISHER
     */
    @PutMapping("/{id}/approve")
    public PublicationRequestResponse approve(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long publisherId
    ) {
        return publicationService.approve(id, publisherId);
    }

    /**
     * Publisher rejects the request
     * Requires ROLE_PUBLISHER
     */
    @PutMapping("/{id}/reject")
    public PublicationRequestResponse reject(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long publisherId,
            @RequestBody @Valid Map<String, @Size(max = 500) String> body
    ) {
        String reason = body.getOrDefault("reason", "Not specified");
        return publicationService.reject(id, publisherId, reason);
    }

    /**
     * Publisher creates the book (publishes it)
     * Requires ROLE_PUBLISHER
     */
    @PostMapping("/{id}/publish")
    public PublicationRequestResponse publish(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long publisherId,
            @RequestBody @Valid Map<String, @NotNull Set<Long>> body
    ) {
        Set<Long> genreIds = body.get("genre_ids");
        if (genreIds == null || genreIds.isEmpty()) {
            throw new IllegalArgumentException("genre_ids is required");
        }
        return publicationService.publish(id, publisherId, genreIds);
    }

    /**
     * Get all publication requests (for publishers)
     */
    @GetMapping
    public ResponseEntity<List<PublicationRequestResponse>> getAllRequests(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        Page<PublicationRequestResponse> p = publicationService.getAllRequests(page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(p.getTotalElements()))
                .body(p.getContent());
    }

    /**
     * Get pending requests (for publishers)
     */
    @GetMapping("/pending")
    public ResponseEntity<List<PublicationRequestResponse>> getPendingRequests(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        Page<PublicationRequestResponse> p = publicationService.getPendingRequests(page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(p.getTotalElements()))
                .body(p.getContent());
    }

    /**
     * Get my publication requests (for users)
     */
    @GetMapping("/my")
    public ResponseEntity<List<PublicationRequestResponse>> getMyRequests(
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        Page<PublicationRequestResponse> p = publicationService.getMyRequests(requesterId, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(p.getTotalElements()))
                .body(p.getContent());
    }
}