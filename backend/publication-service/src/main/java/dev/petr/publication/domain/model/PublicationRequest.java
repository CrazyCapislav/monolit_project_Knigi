package dev.petr.publication.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Publication Request Entity
 * Users request books that don't exist in the system
 * Publishers create these books
 */
@Entity
@Table(name = "publication_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @NotNull
    @Column(nullable = false, length = 500)
    private String title;

    @NotNull
    @Column(nullable = false, length = 200)
    private String author;

    @Column(length = 20)
    private String isbn;

    @Column(name = "published_year")
    private Integer publishedYear;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PublicationStatus status;

    @Column(name = "publisher_id")
    private Long publisherId;

    @Column(name = "created_book_id")
    private Long createdBookId;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
