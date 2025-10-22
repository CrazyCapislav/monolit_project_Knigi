package dev.petr.bookswap.service;

import dev.petr.bookswap.dto.PublicationRequestCreateRequest;
import dev.petr.bookswap.dto.PublicationRequestResponse;
import dev.petr.bookswap.entity.PublicationRequest;
import dev.petr.bookswap.entity.PublicationStatus;
import dev.petr.bookswap.entity.User;
import dev.petr.bookswap.exception.NotFoundException;
import dev.petr.bookswap.repository.PublicationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class PublicationService {

    private final PublicationRequestRepository repo;
    private final UserService userService;

    @Transactional
    public PublicationRequestResponse create(Long requesterId, PublicationRequestCreateRequest req) {
        User requester = userService.getEntity(requesterId);
        User publisher = userService.getEntity(req.publisherId());

        PublicationRequest pr = PublicationRequest.builder().requester(requester).publisher(publisher).title(req.title()).author(req.author()).message(req.message()).status(PublicationStatus.SUBMITTED).createdAt(OffsetDateTime.now()).build();

        return toResponse(repo.save(pr));
    }

    @Transactional
    public PublicationRequestResponse decide(Long requestId, Long publisherId, boolean approve) {
        PublicationRequest pr = repo.findById(requestId).orElseThrow(() -> new NotFoundException("Request not found"));
        if (!pr.getPublisher().getId().equals(publisherId)) throw new IllegalStateException("Not allowed");

        pr.setStatus(approve ? PublicationStatus.APPROVED : PublicationStatus.REJECTED);
        pr.setDecidedAt(OffsetDateTime.now());
        return toResponse(pr);
    }

    @Transactional(readOnly = true)
    public Page<PublicationRequestResponse> page(int page, int size) {
        Page<PublicationRequest> p = repo.findAll(PageRequest.of(page, Math.min(size, 50), Sort.by(Sort.Direction.DESC, "id")));
        return p.map(this::toResponse);
    }

    private PublicationRequestResponse toResponse(PublicationRequest p) {
        return new PublicationRequestResponse(p.getId(), p.getRequester().getId(), p.getPublisher().getId(), p.getTitle(), p.getAuthor(), p.getMessage(), p.getStatus().name(), p.getCreatedAt(), p.getDecidedAt());
    }
}
