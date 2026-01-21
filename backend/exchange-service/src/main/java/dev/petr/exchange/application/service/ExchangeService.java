package dev.petr.exchange.application.service;

import dev.petr.exchange.infrastructure.client.BookServiceClient;
import dev.petr.exchange.application.dto.BookResponse;
import dev.petr.exchange.application.dto.ExchangeRequestCreateRequest;
import dev.petr.exchange.application.dto.ExchangeRequestResponse;
import dev.petr.exchange.domain.event.BookCommandEvent;
import dev.petr.exchange.domain.model.ExchangeRequest;
import dev.petr.exchange.domain.model.ExchangeStatus;
import dev.petr.exchange.domain.event.ExchangeEvent;
import dev.petr.exchange.infrastructure.messaging.producer.BookCommandProducer;
import dev.petr.exchange.infrastructure.messaging.producer.ExchangeEventProducer;
import dev.petr.exchange.application.exception.ServiceUnavailableException;
import dev.petr.exchange.infrastructure.persistence.repository.ExchangeRequestRepository;
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
    private final BookCommandProducer bookCommandProducer;
    private final ExchangeEventProducer eventProducer;

    @Transactional
    @CircuitBreaker(name = "bookService", fallbackMethod = "createFallback")
    public ExchangeRequestResponse create(Long requesterId, ExchangeRequestCreateRequest request) {
        log.info("Creating exchange request from user {} for book {}", requesterId, request.bookRequestedId());

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

        ExchangeEvent event = ExchangeEvent.builder()
                .exchangeId(saved.getId())
                .requesterId(saved.getRequesterId())
                .ownerId(saved.getOwnerId())
                .bookOfferedId(saved.getBookOfferedId())
                .bookRequestedId(saved.getBookRequestedId())
                .status(saved.getStatus().name())
                .build();
        eventProducer.sendExchangeRequested(event);

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

        ExchangeEvent event = ExchangeEvent.builder()
                .exchangeId(updated.getId())
                .requesterId(updated.getRequesterId())
                .ownerId(updated.getOwnerId())
                .bookOfferedId(updated.getBookOfferedId())
                .bookRequestedId(updated.getBookRequestedId())
                .status(updated.getStatus().name())
                .build();
        eventProducer.sendExchangeAccepted(event);

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

        ExchangeEvent event = ExchangeEvent.builder()
                .exchangeId(updated.getId())
                .requesterId(updated.getRequesterId())
                .ownerId(updated.getOwnerId())
                .bookOfferedId(updated.getBookOfferedId())
                .bookRequestedId(updated.getBookRequestedId())
                .status(updated.getStatus().name())
                .build();
        eventProducer.sendExchangeRejected(event);

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

    @SuppressWarnings("unused")
    private BookResponse fetchBookFallback(Long bookId, Long userId, String bookType, Exception e) {
        log.error("Circuit breaker activated for fetchBook: {}", e.getMessage());
        throw new ServiceUnavailableException("Book service is temporarily unavailable. Cannot fetch book information.");
    }

    private void updateBookOwnership(ExchangeRequest exchange, Long ownerId) {
        log.info("Sending ownership update commands for exchange {}", exchange.getId());

        BookCommandEvent requestedUpdate = BookCommandEvent.builder()
                .bookId(exchange.getBookRequestedId())
                .currentOwnerId(ownerId)
                .newOwnerId(exchange.getRequesterId())
                .build();
        bookCommandProducer.sendUpdateOwner(requestedUpdate);

        if (exchange.getBookOfferedId() != null) {
            BookCommandEvent offeredUpdate = BookCommandEvent.builder()
                    .bookId(exchange.getBookOfferedId())
                    .currentOwnerId(exchange.getRequesterId())
                    .newOwnerId(ownerId)
                    .build();
            bookCommandProducer.sendUpdateOwner(offeredUpdate);
        }
    }

    @Transactional(readOnly = true)
    public Page<ExchangeRequestResponse> pageForUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ExchangeRequest> exchanges = exchangeRepository
                .findByRequesterIdOrOwnerId(userId, userId, pageable);
        return exchanges.map(this::toResponse);
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

