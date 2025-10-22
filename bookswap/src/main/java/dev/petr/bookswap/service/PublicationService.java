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

    /**
     * Create a publication request to a publisher.
     * 
     * @Transactional is used for:
     * 1. Loading and validating users (requester, publisher)
     * 2. Creating publication request
     * 
     * Ensures atomicity of request creation with validation
     * of related entities existence.
     */
    @Transactional
    public PublicationRequestResponse create(Long requesterId, PublicationRequestCreateRequest req) {
        User requester = userService.getEntity(requesterId);
        User publisher = userService.getEntity(req.publisherId());

        PublicationRequest pr = PublicationRequest.builder().requester(requester).publisher(publisher).title(req.title()).author(req.author()).message(req.message()).status(PublicationStatus.SUBMITTED).createdAt(OffsetDateTime.now()).build();

        return toResponse(repo.save(pr));
    }

    /**
     * Publisher's decision on publication request (approve/reject).
     * 
     * ⚠️ CRITICAL TRANSACTION ⚠️
     * 
     * @Transactional is necessary to ensure ACID properties:
     * 
     * ATOMICITY:
     * - Update request status (APPROVED or REJECTED)
     * - Set decision timestamp (decidedAt)
     * 
     * Both operations must execute ATOMICALLY.
     * 
     * Critical situation example WITHOUT transaction:
     * 1. Request status changed to APPROVED ✓
     * 2. ⚠️ FAILURE ⚠️ - database unavailable / application error
     * 3. Decision timestamp (decidedAt) NOT set ✗
     * 
     * RESULT: Data in inconsistent state!
     * - Request approved, but no timestamp
     * - Cannot determine when decision was made
     * - Business logic violated (audit trail required)
     * - Potential legal issues in disputes
     * 
     * With transaction: on any error, ALL changes are rolled back,
     * request remains in original state (SUBMITTED).
     * 
     * CONSISTENCY:
     * Ensures status and timestamp are always consistent:
     * - APPROVED/REJECTED -> decidedAt is set
     * - SUBMITTED -> decidedAt = null
     * 
     * ISOLATION:
     * Prevents race condition when two publishers simultaneously
     * try to decide on the same request.
     * 
     * DURABILITY:
     * After transaction commit, decision is irreversibly saved.
     * 
     * @param requestId ID of the publication request
     * @param publisherId ID of the publisher making decision
     * @param approve true to approve, false to reject
     * @return updated request with new status and decision timestamp
     */
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
