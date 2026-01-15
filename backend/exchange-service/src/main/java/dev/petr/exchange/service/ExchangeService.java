package dev.petr.exchange.service;

import dev.petr.exchange.client.BookServiceClient;
import dev.petr.exchange.dto.BookResponse;
import dev.petr.exchange.dto.ExchangeRequestCreateRequest;
import dev.petr.exchange.dto.ExchangeRequestResponse;
import dev.petr.exchange.dto.UpdateBookOwnerRequest;
import dev.petr.exchange.entity.ExchangeRequest;
import dev.petr.exchange.entity.ExchangeStatus;
import dev.petr.exchange.repository.ExchangeRequestRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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
    @CircuitBreaker(name = "bookService", fallbackMethod = "createFallback")
    public ExchangeRequestResponse create(Long requesterId, ExchangeRequestCreateRequest request) {
        log.info("Creating exchange request from user {} for book {}", requesterId, request.bookRequestedId());

        BookResponse requestedBook = fetchBook(request.bookRequestedId(), requesterId, "Requested book");

        if (requestedBook == null || "UNKNOWN".equals(requestedBook.status())) {
            throw new IllegalStateException("Book service is temporarily unavailable. Please try again later.");
        }

        if (!"AVAILABLE".equals(requestedBook.status())) {
            throw new IllegalArgumentException("Book is not available for exchange");
        }

        if (requestedBook.ownerId().equals(requesterId)) {
            throw new IllegalArgumentException("Cannot request your own book");
        }

        if (request.bookOfferedId() != null) {
            BookResponse offeredBook = fetchBook(request.bookOfferedId(), requesterId, "Offered book");

            if ("UNKNOWN".equals(offeredBook.status())) {
                throw new IllegalStateException("Book service is temporarily unavailable. Please try again later.");
            }
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
    @CircuitBreaker(name = "bookService", fallbackMethod = "acceptFallback")
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
     * Circuit Breaker will handle failures automatically.
     */
    @CircuitBreaker(name = "bookService", fallbackMethod = "fetchBookFallback")
    private BookResponse fetchBook(Long bookId, Long userId, String bookType) {
        BookResponse book = bookServiceClient.getBook(bookId, userId);
        log.info("Successfully fetched {} with id {} from Book Service", bookType, bookId);
        return book;
    }

    /**
     * Update book ownership during exchange.
     * Circuit Breaker will handle failures automatically.
     */
    @CircuitBreaker(name = "bookService", fallbackMethod = "updateBookOwnershipFallback")
    private void updateBookOwnership(ExchangeRequest exchange, Long ownerId) {
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

    // Fallback methods for Circuit Breaker

    public ExchangeRequestResponse createFallback(Long requesterId, ExchangeRequestCreateRequest request, Exception e) {
        log.error("Circuit breaker activated in create method. Book Service unavailable.", e);
        throw new IllegalStateException("Book service is temporarily unavailable. Please try again later.");
    }

    public ExchangeRequestResponse acceptFallback(Long exchangeId, Long ownerId, Exception e) {
        log.error("Circuit breaker activated in accept method. Book Service unavailable.", e);
        throw new IllegalStateException("Book service is temporarily unavailable. Cannot complete exchange.");
    }

    private BookResponse fetchBookFallback(Long bookId, Long userId, String bookType, Exception e) {
        log.error("Circuit breaker activated in fetchBook method. Book Service unavailable for {} {}. Error: {}", 
                bookType, bookId, e.getMessage());
        throw new IllegalStateException("Book service is temporarily unavailable. Please try again later.");
    }

    private void updateBookOwnershipFallback(ExchangeRequest exchange, Long ownerId, Exception e) {
        log.error("Circuit breaker activated in updateBookOwnership method. Book Service unavailable.", e);
        throw new IllegalStateException("Failed to complete exchange. Book service may be unavailable.");
    }
}