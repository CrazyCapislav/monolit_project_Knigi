package dev.petr.bookswap.controller;

import dev.petr.bookswap.dto.*;
import dev.petr.bookswap.service.PublicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController @RequestMapping("/api/v1/publications")
public class PublicationController {
    private final PublicationService service;
    public PublicationController(PublicationService service){ this.service = service; }

    @PostMapping
    public ResponseEntity<PublicationRequestResponse> create(
            @RequestHeader("X-User-Id") Long requesterId,
            @Valid @RequestBody PublicationRequestCreateRequest req){
        PublicationRequestResponse r = service.create(requesterId,req);
        return ResponseEntity.created(URI.create("/api/v1/publications/"+r.id())).body(r);
    }

    @PostMapping("/{id}/approve")
    public PublicationRequestResponse approve(@PathVariable Long id,
                                              @RequestHeader("X-User-Id") Long publisherId){
        return service.decide(id,publisherId,true);
    }
    @PostMapping("/{id}/reject")
    public PublicationRequestResponse reject(@PathVariable Long id,
                                             @RequestHeader("X-User-Id") Long publisherId){
        return service.decide(id,publisherId,false);
    }

    @GetMapping
    public ResponseEntity<List<PublicationRequestResponse>> page(
            @RequestParam(defaultValue="0") @Min(0) int page,
            @RequestParam(defaultValue="20") @Min(1) @Max(50) int size){
        Page<PublicationRequestResponse> p = service.page(page,size);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(p.getTotalElements()))
                .body(p.getContent());
    }
}
