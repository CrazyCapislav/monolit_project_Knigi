package dev.petr.bookswap.service;

import dev.petr.bookswap.dto.*;
import dev.petr.bookswap.entity.*;
import dev.petr.bookswap.exception.NotFoundException;
import dev.petr.bookswap.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class PublicationService {
    private final PublicationRequestRepository repo;
    private final UserRepository userRepo;

    public PublicationService(PublicationRequestRepository repo, UserRepository userRepo) {
        this.repo = repo; this.userRepo = userRepo;
    }

    @Transactional
    public PublicationRequestResponse create(Long requesterId, PublicationRequestCreateRequest req) {
        User requester = userRepo.findById(requesterId)
                .orElseThrow(() -> new NotFoundException("Requester not found"));
        User publisher = userRepo.findById(req.publisherId())
                .orElseThrow(() -> new NotFoundException("Publisher not found"));

        PublicationRequest pr = PublicationRequest.builder()
                .requester(requester)
                .publisher(publisher)
                .title(req.title())
                .author(req.author())
                .message(req.message())
                .status(PublicationStatus.SUBMITTED)
                .createdAt(OffsetDateTime.now())
                .build();

        return toResponse(repo.save(pr));
    }

    @Transactional
    public PublicationRequestResponse decide(Long requestId, Long publisherId, boolean approve) {
        PublicationRequest pr = repo.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found"));
        if (!pr.getPublisher().getId().equals(publisherId))
            throw new IllegalStateException("Not allowed");

        pr.setStatus(approve ? PublicationStatus.APPROVED : PublicationStatus.REJECTED);
        pr.setDecidedAt(OffsetDateTime.now());
        return toResponse(pr);
    }

    public Page<PublicationRequestResponse> page(int page, int size) {
        Page<PublicationRequest> p = repo.findAll(
                PageRequest.of(page,Math.min(size,50),Sort.by(Sort.Direction.DESC,"id")));
        return p.map(this::toResponse);
    }

    private PublicationRequestResponse toResponse(PublicationRequest p){
        return new PublicationRequestResponse(
                p.getId(), p.getRequester().getId(), p.getPublisher().getId(),
                p.getTitle(), p.getAuthor(), p.getMessage(),
                p.getStatus().name(), p.getCreatedAt(), p.getDecidedAt());
    }
}
