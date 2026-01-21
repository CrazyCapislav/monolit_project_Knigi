package dev.petr.publication.application.service;

import dev.petr.publication.infrastructure.messaging.producer.BookCommandProducer;
import dev.petr.publication.infrastructure.messaging.producer.PublicationEventProducer;
import dev.petr.publication.application.dto.PublicationRequestCreateRequest;
import dev.petr.publication.domain.model.PublicationRequest;
import dev.petr.publication.domain.model.PublicationStatus;
import dev.petr.publication.infrastructure.persistence.repository.PublicationRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicationServiceTest {

    @Mock
    private PublicationRequestRepository publicationRepository;

    @Mock
    private BookCommandProducer bookCommandProducer;

    @Mock
    private PublicationEventProducer publicationEventProducer;

    @InjectMocks
    private PublicationService publicationService;

    private PublicationRequest testRequest;
    @BeforeEach
    void setUp() {
        testRequest = PublicationRequest.builder()
                .id(1L)
                .requesterId(1L)
                .title("New Book")
                .author("Author")
                .isbn("123")
                .status(PublicationStatus.PENDING)
                .createdAt(OffsetDateTime.now())
                .build();

    }

    @Test
    void requestBook_Success() {
        
        PublicationRequestCreateRequest request = new PublicationRequestCreateRequest(
                "New Book", "Author", "123", 2024, "Description"
        );

        when(publicationRepository.save(any(PublicationRequest.class))).thenReturn(testRequest);

        
        var response = publicationService.requestBook(1L, request);

        
        assertNotNull(response);
        assertEquals(PublicationStatus.PENDING.name(), response.status());
        verify(publicationRepository).save(any(PublicationRequest.class));
    }

    @Test
    void approve_Success() {
        
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(publicationRepository.save(any(PublicationRequest.class))).thenReturn(testRequest);

        
        var response = publicationService.approve(1L, 2L);

        
        assertNotNull(response);
        assertEquals(PublicationStatus.APPROVED.name(), response.status());
        assertEquals(2L, response.publisherId());
        verify(publicationRepository).save(any(PublicationRequest.class));
    }

    @Test
    void approve_AlreadyApproved_ThrowsException() {
        
        testRequest.setStatus(PublicationStatus.APPROVED);
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(testRequest));

         
        assertThrows(IllegalArgumentException.class,
                () -> publicationService.approve(1L, 2L));

        verify(publicationRepository, never()).save(any());
    }

    @Test
    void reject_Success() {
        
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(publicationRepository.save(any(PublicationRequest.class))).thenReturn(testRequest);

        
        var response = publicationService.reject(1L, 2L, "Not suitable");

        
        assertNotNull(response);
        assertEquals(PublicationStatus.REJECTED.name(), response.status());
        assertEquals("Not suitable", response.rejectionReason());
    }

    @Test
    void publish_Success_CreatesBook() {
        
        testRequest.setStatus(PublicationStatus.APPROVED);
        testRequest.setPublisherId(2L);

        when(publicationRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(publicationRepository.save(any(PublicationRequest.class))).thenReturn(testRequest);

        
        var response = publicationService.publish(1L, 2L, Set.of(1L));

        
        assertNotNull(response);
        assertEquals(PublicationStatus.PUBLISHING.name(), response.status());

        verify(bookCommandProducer).sendCreateBook(any());
        verify(publicationRepository).save(any(PublicationRequest.class));
    }

    @Test
    void publish_NotApproved_ThrowsException() {
        
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(testRequest));

         
        assertThrows(IllegalArgumentException.class,
                () -> publicationService.publish(1L, 2L, Set.of(1L)));

        verify(bookCommandProducer, never()).sendCreateBook(any());
    }

    @Test
    void publish_WrongPublisher_ThrowsException() {
        
        testRequest.setStatus(PublicationStatus.APPROVED);
        testRequest.setPublisherId(2L);
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(testRequest));

         
        assertThrows(IllegalArgumentException.class,
                () -> publicationService.publish(1L, 999L, Set.of(1L)));

        verify(bookCommandProducer, never()).sendCreateBook(any());
    }
}
