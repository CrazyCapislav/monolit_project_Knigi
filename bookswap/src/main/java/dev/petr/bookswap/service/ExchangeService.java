package dev.petr.bookswap.service;

import dev.petr.bookswap.dto.ExchangeRequestCreateRequest;
import dev.petr.bookswap.dto.ExchangeRequestResponse;
import dev.petr.bookswap.entity.*;
import dev.petr.bookswap.exception.NotFoundException;
import dev.petr.bookswap.repository.ExchangeRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final ExchangeRequestRepository repo;
    private final BookService bookService;
    private final UserService userService;

    /**
     * Create an exchange request for book swap.
     *
     * @Transactional is used here for the following reasons:
     * 1. Loading related entities (User, Book) from database
     * 2. Validating the owner of the offered book
     * 3. Creating new ExchangeRequest record
     * <p>
     * All operations must be atomic: if validation fails,
     * no changes should be made to the database.
     */
    @Transactional
    public ExchangeRequestResponse create(Long requesterId, ExchangeRequestCreateRequest req) {
        User requester = userService.getEntity(requesterId);
        Book requested = bookService.getEntity(req.bookRequestedId());
        User owner = requested.getOwner();

        if (owner.getId().equals(requesterId)) {
            throw new IllegalArgumentException("Cannot create exchange request for your own book");
        }

        Book offered = null;
        if (req.bookOfferedId() != null) {
            offered = bookService.getEntity(req.bookOfferedId());
            if (!offered.getOwner().getId().equals(requesterId))
                throw new IllegalStateException("Offered book does not belong to requester");
        }

        ExchangeRequest er = ExchangeRequest.builder().requester(requester).owner(owner).bookRequested(requested)
                .bookOffered(offered).status(ExchangeStatus.WAITING).createdAt(OffsetDateTime.now()).build();

        return toResponse(repo.save(er));
    }

    @Transactional
    public ExchangeRequestResponse accept(Long exchangeId, Long ownerId) {
        ExchangeRequest er = repo.findById(exchangeId).orElseThrow(() -> new NotFoundException("Exchange not found"));
        if (!er.getOwner().getId().equals(ownerId)) throw new IllegalStateException("Not an owner");

        Book requestedBook = er.getBookRequested();
        requestedBook.setOwner(er.getRequester());
        requestedBook.setStatus(BookStatus.EXCHANGED);

        if (er.getBookOffered() != null) {
            Book offeredBook = er.getBookOffered();
            offeredBook.setOwner(er.getOwner());
            offeredBook.setStatus(BookStatus.EXCHANGED);
        }

        er.setStatus(ExchangeStatus.ACCEPTED);
        er.setUpdatedAt(OffsetDateTime.now());

        return toResponse(er);
    }

    @Transactional(readOnly = true)
    public Page<ExchangeRequestResponse> page(int page, int size) {
        Page<ExchangeRequest> p =
                repo.findAll(PageRequest.of(page, Math.min(size, 50), Sort.by(Sort.Direction.DESC, "id")));
        return p.map(this::toResponse);
    }

    private ExchangeRequestResponse toResponse(ExchangeRequest e) {
        return new ExchangeRequestResponse(e.getId(), e.getRequester().getId(), e.getOwner().getId(),
                e.getBookRequested().getId(), e.getBookOffered() == null ? null : e.getBookOffered().getId(),
                e.getStatus().name(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
