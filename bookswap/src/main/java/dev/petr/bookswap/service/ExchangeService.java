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
public class ExchangeService {
    private final ExchangeRequestRepository repo;
    private final BookRepository bookRepo;
    private final UserRepository userRepo;

    public ExchangeService(ExchangeRequestRepository repo, BookRepository bookRepo, UserRepository userRepo) {
        this.repo = repo; this.bookRepo = bookRepo; this.userRepo = userRepo;
    }

    @Transactional
    public ExchangeRequestResponse create(Long requesterId, ExchangeRequestCreateRequest req) {
        User requester = userRepo.findById(requesterId)
                .orElseThrow(() -> new NotFoundException("Requester not found"));
        Book requested = bookRepo.findById(req.bookRequestedId())
                .orElseThrow(() -> new NotFoundException("Book requested not found"));
        User owner = requested.getOwner();

        Book offered = null;
        if (req.bookOfferedId() != null) {
            offered = bookRepo.findById(req.bookOfferedId())
                    .orElseThrow(() -> new NotFoundException("Offered book not found"));
            if (!offered.getOwner().getId().equals(requesterId))
                throw new IllegalStateException("Offered book does not belong to requester");
        }

        ExchangeRequest er = ExchangeRequest.builder()
                .requester(requester)
                .owner(owner)
                .bookRequested(requested)
                .bookOffered(offered)
                .status(ExchangeStatus.WAITING)
                .createdAt(OffsetDateTime.now())
                .build();

        return toResponse(repo.save(er));
    }

    @Transactional
    public ExchangeRequestResponse accept(Long exchangeId, Long ownerId) {
        ExchangeRequest er = repo.findById(exchangeId)
                .orElseThrow(() -> new NotFoundException("Exchange not found"));
        if (!er.getOwner().getId().equals(ownerId))
            throw new IllegalStateException("Not an owner");

        er.setStatus(ExchangeStatus.ACCEPTED);
        er.setUpdatedAt(OffsetDateTime.now());
        er.getBookRequested().setStatus(BookStatus.EXCHANGED);
        if (er.getBookOffered() != null) er.getBookOffered().setStatus(BookStatus.EXCHANGED);

        return toResponse(er);
    }

    public Page<ExchangeRequestResponse> page(int page, int size) {
        Page<ExchangeRequest> p = repo.findAll(
                PageRequest.of(page, Math.min(size,50), Sort.by(Sort.Direction.DESC,"id")));
        return p.map(this::toResponse);
    }

    private ExchangeRequestResponse toResponse(ExchangeRequest e) {
        return new ExchangeRequestResponse(
                e.getId(), e.getRequester().getId(), e.getOwner().getId(),
                e.getBookRequested().getId(),
                e.getBookOffered() == null ? null : e.getBookOffered().getId(),
                e.getStatus().name(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
