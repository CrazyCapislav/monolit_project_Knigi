package dev.petr.exchange.service;

import dev.petr.exchange.client.BookServiceClient;
import dev.petr.exchange.dto.BookResponse;
import dev.petr.exchange.dto.ExchangeRequestCreateRequest;
import dev.petr.exchange.dto.ExchangeRequestResponse;
import dev.petr.exchange.dto.UpdateBookOwnerRequest;
import dev.petr.exchange.entity.ExchangeRequest;
import dev.petr.exchange.entity.ExchangeStatus;
import dev.petr.exchange.repository.ExchangeRequestRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final ExchangeRequestRepository exchangeRepository;
    private final BookServiceClient bookServiceClient;

    @Transactional
    public ExchangeRequestResponse create(Long requesterId, ExchangeRequestCreateRequest request) {
        log.info("Creating exchange request from user {} for book {}", requesterId, request.bookRequestedId());

        // Fetch requested book
        BookResponse requestedBook = fetchBook(request.bookRequestedId(), requesterId, "Requested book");

        if (requestedBook == null || "UNKNOWN".equals(requestedBook.status())) {
            throw new IllegalArgumentException("Requested book not found or unavailable");
        }

        if (!"AVAILABLE".equals(requestedBook.status())) {
            throw new IllegalArgumentException("Book is not available for exchange");
        }

        if (requestedBook.ownerId().equals(requesterId)) {
            throw new IllegalArgumentException("Cannot request your own book");
        }

        if (request.bookOfferedId() != null) {
            BookResponse offeredBook = fetchBook(request.bookOfferedId(), requesterId, "Offered book");

            if (!offeredBook.ownerId().equals(requesterId)) {
                throw new IllegalArgumentException("You can only offer your own books");
            }
            if (!"AVAILABLE".equals(offeredBook.status())) {
                throw new IllegalArgumentException("Offered book is not available");
            }
        }

        ExchangeRequest exchange = ExchangeRequest.builder()
                .requesterId(requesterId)
                .ownerId(requestedBook.ownerId())
                .bookRequestedId(request.bookRequestedId())
                .bookOfferedId(request.bookOfferedId())
                .status(ExchangeStatus.WAITING)
                .createdAt(OffsetDateTime.now())
                .build();

        ExchangeRequest saved = exchangeRepository.save(exchange);
        log.info("Exchange request {} created successfully", saved.getId());

        return toResponse(saved);
    }

    @Transactional
    public ExchangeRequestResponse accept(Long exchangeId, Long ownerId) {
        log.info("User {} accepting exchange request {}", ownerId, exchangeId);

        ExchangeRequest exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new IllegalArgumentException("Exchange request not found"));

        if (!exchange.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Only the book owner can accept this exchange");
        }

        if (exchange.getStatus() != ExchangeStatus.WAITING) {
            throw new IllegalArgumentException("Exchange request is not in WAITING status");
        }

        updateBookOwnership(exchange, ownerId);

        exchange.setStatus(ExchangeStatus.ACCEPTED);
        exchange.setUpdatedAt(OffsetDateTime.now());

        ExchangeRequest updated = exchangeRepository.save(exchange);
        log.info("Exchange request {} accepted and ownership swapped", exchangeId);

        return toResponse(updated);
    }

    @Transactional(readOnly = true)
    public Page<ExchangeRequestResponse> page(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return exchangeRepository.findAll(pageable).map(this::toResponse);
    }

    /**
     * Fetch book from Book Service.
     * Lets FeignException (4xx errors) propagate to GlobalExceptionHandler.
     * Only catches connectivity/timeout issues.
     */
    private BookResponse fetchBook(Long bookId, Long userId, String bookType) {
        try {
            BookResponse book = bookServiceClient.getBook(bookId, userId);
            log.info("Successfully fetched {} with id {} from Book Service", bookType, bookId);
            return book;
        } catch (FeignException e) {
            log.warn("Feign error fetching {}: {} - {}", bookType, e.status(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch {} from Book Service", bookType, e);
            throw new IllegalStateException("Book service is temporarily unavailable. Please try again later.");
        }
    }

    /**
     * Update book ownership during exchange.
     * Lets FeignException propagate for proper error handling.
     */
    private void updateBookOwnership(ExchangeRequest exchange, Long ownerId) {
        try {
            log.info("Swapping ownership: book {} owner {} -> {}, book {} owner {} -> {}",
                    exchange.getBookRequestedId(), ownerId, exchange.getRequesterId(),
                    exchange.getBookOfferedId(), exchange.getRequesterId(), ownerId);

            bookServiceClient.updateOwner(
                    exchange.getBookRequestedId(),
                    new UpdateBookOwnerRequest(exchange.getRequesterId()),
                    ownerId
            );

            if (exchange.getBookOfferedId() != null) {
                bookServiceClient.updateOwner(
                        exchange.getBookOfferedId(),
                        new UpdateBookOwnerRequest(ownerId),
                        exchange.getRequesterId()
                );
            }

            log.info("Book ownership successfully swapped");
        } catch (FeignException e) {
            log.error("Feign error updating book ownership: {} - {}", e.status(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to update book ownership", e);
            throw new IllegalStateException("Failed to complete exchange. Book service may be unavailable.");
        }
    }

    private ExchangeRequestResponse toResponse(ExchangeRequest exchange) {
        return new ExchangeRequestResponse(
                exchange.getId(),
                exchange.getRequesterId(),
                exchange.getOwnerId(),
                exchange.getBookRequestedId(),
                exchange.getBookOfferedId(),
                exchange.getStatus().name(),
                exchange.getCreatedAt(),
                exchange.getUpdatedAt()
        );
    }
}