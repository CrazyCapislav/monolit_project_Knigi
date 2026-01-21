package dev.petr.publication.presentation.controller;

import dev.petr.publication.application.dto.PublicationRequestCreateRequest;
import dev.petr.publication.application.dto.PublicationRequestResponse;
import dev.petr.publication.application.service.PublicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/v1/publications")
@RequiredArgsConstructor
public class PublicationController {

    private final PublicationService publicationService;

    /**
     * User requests a book publication
     * Only USER role can request publications
     */
    @PostMapping("/request")
    @ResponseStatus(HttpStatus.CREATED)
    public PublicationRequestResponse requestBook(
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody PublicationRequestCreateRequest request
    ) {
        if (role == null || !role.equals("ROLE_USER")) {
            throw new IllegalArgumentException("Only users can request publications");
        }

        log.info("User {} requesting publication: {}", requesterId, request.title());
        return publicationService.requestBook(requesterId, request);
    }

    /**
     * Publisher approves a publication request
     * Only PUBLISHER role can approve
     */
    @PutMapping("/{id}/approve")
    public PublicationRequestResponse approve(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long publisherId,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        if (role == null || !role.equals("ROLE_PUBLISHER")) {
            throw new IllegalArgumentException("Only publishers can approve publication requests");
        }

        log.info("Publisher {} approving publication request {}", publisherId, id);
        return publicationService.approve(id, publisherId);
    }

    /**
     * Publisher rejects a publication request
     * Only PUBLISHER role can reject
     */
    @PutMapping("/{id}/reject")
    public PublicationRequestResponse reject(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long publisherId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestBody @Valid Map<String, @Size(max = 500) String> body
    ) {
        if (role == null || !role.equals("ROLE_PUBLISHER")) {
            throw new IllegalArgumentException("Only publishers can reject publication requests");
        }

        String reason = body.getOrDefault("reason", "Not specified");
        log.info("Publisher {} rejecting publication request {} with reason: {}",
                publisherId, id, reason);
        return publicationService.reject(id, publisherId, reason);
    }

    /**
     * Publisher creates the book (publishes it)
     * Only PUBLISHER role can publish
     */
    @PostMapping("/{id}/publish")
    public PublicationRequestResponse publish(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long publisherId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestBody @Valid Map<String, @NotNull Set<Long>> body
    ) {
        if (role == null || !role.equals("ROLE_PUBLISHER")) {
            throw new IllegalArgumentException("Only publishers can publish books");
        }

        Set<Long> genreIds = body.get("genre_ids");
        if (genreIds == null || genreIds.isEmpty()) {
            throw new IllegalArgumentException("genre_ids is required");
        }

        log.info("Publisher {} publishing book for request {}", publisherId, id);
        return publicationService.publish(id, publisherId, genreIds);
    }

    /**
     * Get all publication requests
     * ADMIN and PUBLISHER can view all requests
     */
    @GetMapping
    public ResponseEntity<List<PublicationRequestResponse>> getAllRequests(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        if (role == null || (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_PUBLISHER"))) {
            throw new IllegalArgumentException("Access denied");
        }

        Page<PublicationRequestResponse> p = publicationService.getAllRequests(page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(p.getTotalElements()))
                .body(p.getContent());
    }

    /**
     * Get pending publication requests
     * Only PUBLISHER can view pending requests
     */
    @GetMapping("/pending")
    public ResponseEntity<List<PublicationRequestResponse>> getPendingRequests(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestHeader(value = "X-User-Role", required = false) String role
    ) {
        if (role == null || !role.equals("ROLE_PUBLISHER")) {
            throw new IllegalArgumentException("Only publishers can view pending requests");
        }

        Page<PublicationRequestResponse> p = publicationService.getPendingRequests(page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(p.getTotalElements()))
                .body(p.getContent());
    }

    /**
     * Get current user's publication requests
     * USER can view their own publication requests
     */
    @GetMapping("/my")
    public ResponseEntity<List<PublicationRequestResponse>> getMyRequests(
            @RequestHeader("X-User-Id") Long requesterId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
    ) {
        if (role == null || !role.equals("ROLE_USER")) {
            throw new IllegalArgumentException("Access denied");
        }

        log.debug("User {} fetching own publication requests", requesterId);
        Page<PublicationRequestResponse> p = publicationService.getMyRequests(requesterId, page, size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(p.getTotalElements()))
                .body(p.getContent());
    }
}
