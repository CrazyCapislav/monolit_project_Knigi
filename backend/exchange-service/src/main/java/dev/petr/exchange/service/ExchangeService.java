package dev.petr.exchange.service;

import dev.petr.exchange.client.BookServiceClient;
import dev.petr.exchange.dto.BookResponse;
import dev.petr.exchange.dto.ExchangeRequestCreateRequest;
import dev.petr.exchange.dto.ExchangeRequestResponse;
import dev.petr.exchange.dto.UpdateBookOwnerRequest;
import dev.petr.exchange.entity.ExchangeRequest;
import dev.petr.exchange.entity.ExchangeStatus;
import dev.petr.exchange.exception.ServiceUnavailableException;
import dev.petr.exchange.repository.ExchangeRequestRepository;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
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

        // Early validation before calling Book Service
        if (request.bookOfferedId() != null && request.bookOfferedId().equals(request.bookRequestedId())) {
            throw new IllegalArgumentException("Cannot offer the same book that you are requesting");
        }

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

    public ExchangeRequestResponse createFallback(Long requesterId, ExchangeRequestCreateRequest request, Exception e) {
        log.error("Book Service unavailable, cannot create exchange: {}", e.getMessage());
        throw new ServiceUnavailableException("Book service is temporarily unavailable. Cannot create exchange request.");
    }

    @Transactional
    @CircuitBreaker(name = "bookService", fallbackMethod = "acceptFallback")
    @Retry(name = "bookService")
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

    @Transactional
    public ExchangeRequestResponse acceptFallback(Long exchangeId, Long ownerId, Exception e) {
        log.error("Book Service unavailable, cannot complete exchange {}: {}", exchangeId, e.getMessage());

        ExchangeRequest exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new IllegalArgumentException("Exchange request not found"));

        exchange.setStatus(ExchangeStatus.WAITING);
        exchangeRepository.save(exchange);

        throw new ServiceUnavailableException("Book service is temporarily unavailable. Exchange could not be completed.");
    }

    @Transactional
    public ExchangeRequestResponse reject(Long exchangeId, Long ownerId) {
        log.info("User {} rejecting exchange request {}", ownerId, exchangeId);

        ExchangeRequest exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new IllegalArgumentException("Exchange request not found"));

        if (!exchange.getOwnerId().equals(ownerId)) {
            throw new IllegalArgumentException("Only the book owner can reject this exchange");
        }

        if (exchange.getStatus() != ExchangeStatus.WAITING) {
            throw new IllegalArgumentException("Exchange request is not in WAITING status");
        }

        exchange.setStatus(ExchangeStatus.DECLINED);
        exchange.setUpdatedAt(OffsetDateTime.now());

        ExchangeRequest updated = exchangeRepository.save(exchange);
        log.info("Exchange request {} rejected", exchangeId);

        return toResponse(updated);
    }

    @Transactional(readOnly = true)
    public Page<ExchangeRequestResponse> page(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return exchangeRepository.findAll(pageable).map(this::toResponse);
    }

    @CircuitBreaker(name = "bookService", fallbackMethod = "fetchBookFallback")
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

    private BookResponse fetchBookFallback(Long bookId, Long userId, String bookType, Exception e) {
        log.error("Circuit breaker activated for fetchBook: {}", e.getMessage());
        throw new ServiceUnavailableException("Book service is temporarily unavailable. Cannot fetch book information.");
    }

    @CircuitBreaker(name = "bookService", fallbackMethod = "updateBookOwnershipFallback")
    private void updateBookOwnership(ExchangeRequest exchange, Long ownerId) {
        try {
            log.info("Swapping ownership: book {} owner {} -> {}, book {} owner {} -> {}",
                    exchange.getBookRequestedId(), ownerId, exchange.getRequesterId(),
                    exchange.getBookOfferedId(), exchange.getRequesterId(), ownerId);

            bookServiceClient.updateOwner(
                    exchange.getBookRequestedId(),
                    new UpdateBookOwnerRequest(exchange.getRequesterId()),
                    ownerId,
                    "ROLE_USER"
            );

            if (exchange.getBookOfferedId() != null) {
                bookServiceClient.updateOwner(
                        exchange.getBookOfferedId(),
                        new UpdateBookOwnerRequest(ownerId),
                        exchange.getRequesterId(),
                        "ROLE_USER"
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

    private void updateBookOwnershipFallback(ExchangeRequest exchange, Long ownerId, Exception e) {
        log.error("Circuit breaker activated for updateBookOwnership: {}", e.getMessage());
        throw new ServiceUnavailableException("Book service is temporarily unavailable. Cannot update book ownership.");
    }

    @Transactional(readOnly = true)
    public Page<ExchangeRequestResponse> pageForUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ExchangeRequest> exchanges = exchangeRepository
                .findByRequesterIdOrOwnerId(userId, userId, pageable);
        return exchanges.map(this::toResponse);
    }

//    @Transactional
//    public ExchangeRequestResponse cancelByAdmin(Long exchangeId) {
//        log.info("Admin canceling exchange request {}", exchangeId);
//
//        ExchangeRequest exchange = exchangeRepository.findById(exchangeId)
//                .orElseThrow(() -> new IllegalArgumentException("Exchange request not found"));
//
//        if (exchange.getStatus() != ExchangeStatus.WAITING) {
//            throw new IllegalArgumentException("Can only cancel WAITING exchange requests");
//        }
//
//        exchange.setStatus(ExchangeStatus.DECLINED);
//        exchange.setUpdatedAt(OffsetDateTime.now());
//
//        ExchangeRequest updated = exchangeRepository.save(exchange);
//        log.info("Exchange request {} canceled by admin", exchangeId);
//
//        return toResponse(updated);
//    }

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
