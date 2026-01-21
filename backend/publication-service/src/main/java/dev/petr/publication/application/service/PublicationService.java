package dev.petr.publication.application.service;

import dev.petr.publication.application.dto.PublicationRequestCreateRequest;
import dev.petr.publication.application.dto.PublicationRequestResponse;
import dev.petr.publication.domain.event.BookCommandEvent;
import dev.petr.publication.domain.event.PublicationEvent;
import dev.petr.publication.domain.model.PublicationRequest;
import dev.petr.publication.domain.model.PublicationStatus;
import dev.petr.publication.infrastructure.messaging.producer.BookCommandProducer;
import dev.petr.publication.infrastructure.messaging.producer.PublicationEventProducer;
import dev.petr.publication.infrastructure.persistence.repository.PublicationRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicationService {

    private final PublicationRequestRepository publicationRepository;
    private final BookCommandProducer bookCommandProducer;
    private final PublicationEventProducer eventProducer;

    @Transactional
    public PublicationRequestResponse requestBook(Long requesterId, PublicationRequestCreateRequest request) {
        log.info("User {} requesting book: {}", requesterId, request.title());

        PublicationRequest pubRequest = PublicationRequest.builder()
                .requesterId(requesterId)
                .title(request.title())
                .author(request.author())
                .isbn(request.isbn())
                .publishedYear(request.publishedYear())
                .description(request.description())
                .status(PublicationStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .build();

        PublicationRequest saved = publicationRepository.save(pubRequest);
        log.info("Publication request {} created successfully", saved.getId());

        return toResponse(saved);
    }

    @Transactional
    public PublicationRequestResponse approve(Long requestId, Long publisherId) {
        log.info("Publisher {} approving publication request {}", publisherId, requestId);

        PublicationRequest request = publicationRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Publication request not found"));

        if (request.getStatus() != PublicationStatus.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be approved");
        }

        request.setStatus(PublicationStatus.APPROVED);
        request.setPublisherId(publisherId);
        request.setUpdatedAt(OffsetDateTime.now());

        PublicationRequest updated = publicationRepository.save(request);
        log.info("Publication request {} approved by publisher {}", requestId, publisherId);

        PublicationEvent event = PublicationEvent.builder()
                .publicationId(updated.getId())
                .userId(updated.getRequesterId())
                .bookId(null)
                .status(updated.getStatus().name())
                .build();
        eventProducer.sendPublicationApproved(event);

        return toResponse(updated);
    }

    @Transactional
    public PublicationRequestResponse reject(Long requestId, Long publisherId, String reason) {
        log.info("Publisher {} rejecting publication request {}", publisherId, requestId);

        PublicationRequest request = publicationRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Publication request not found"));

        if (request.getStatus() != PublicationStatus.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be rejected");
        }

        request.setStatus(PublicationStatus.REJECTED);
        request.setPublisherId(publisherId);
        request.setRejectionReason(reason);
        request.setUpdatedAt(OffsetDateTime.now());

        PublicationRequest updated = publicationRepository.save(request);
        log.info("Publication request {} rejected by publisher {}", requestId, publisherId);

        PublicationEvent event = PublicationEvent.builder()
                .publicationId(updated.getId())
                .userId(updated.getRequesterId())
                .bookId(null)
                .status(updated.getStatus().name())
                .build();
        eventProducer.sendPublicationRejected(event);

        return toResponse(updated);
    }

    @Transactional
    public PublicationRequestResponse publish(Long requestId, Long publisherId, Set<Long> genreIds) {
        log.info("Publisher {} publishing book for request {}", publisherId, requestId);

        PublicationRequest request = publicationRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Publication request not found"));

        if (request.getStatus() != PublicationStatus.APPROVED) {
            throw new IllegalArgumentException("Only approved requests can be published");
        }

        if (!publisherId.equals(request.getPublisherId())) {
            throw new IllegalArgumentException("Only the assigned publisher can publish this book");
        }

        BookCommandEvent command = BookCommandEvent.builder()
                .publicationRequestId(request.getId())
                .ownerId(publisherId)
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .publishedYear(request.getPublishedYear())
                .condition("NEW")
                .genreIds(genreIds)
                .build();
        bookCommandProducer.sendCreateBook(command);

        request.setStatus(PublicationStatus.PUBLISHING);
        request.setUpdatedAt(OffsetDateTime.now());

        PublicationRequest updated = publicationRepository.save(request);
        log.info("Publication request {} sent to Book Service for creation", requestId);

        return toResponse(updated);
    }

    @Transactional(readOnly = true)
    public Page<PublicationRequestResponse> getAllRequests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return publicationRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PublicationRequestResponse> getPendingRequests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return publicationRepository.findAllByStatus(PublicationStatus.PENDING, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<PublicationRequestResponse> getMyRequests(Long requesterId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return publicationRepository.findAllByRequesterId(requesterId, pageable)
                .map(this::toResponse);
    }

    private PublicationRequestResponse toResponse(PublicationRequest request) {
        return new PublicationRequestResponse(
                request.getId(),
                request.getRequesterId(),
                request.getTitle(),
                request.getAuthor(),
                request.getIsbn(),
                request.getPublishedYear(),
                request.getDescription(),
                request.getStatus().name(),
                request.getPublisherId(),
                request.getCreatedBookId(),
                request.getRejectionReason(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
}
