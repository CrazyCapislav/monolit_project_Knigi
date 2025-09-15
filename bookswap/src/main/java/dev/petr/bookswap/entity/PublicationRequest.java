package dev.petr.bookswap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity @Table(name="publication_request")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PublicationRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="requester_id")
    private User requester;

    @ManyToOne(optional=false) @JoinColumn(name="publisher_id")
    private User publisher;                 // user with PUBLISHER role

    @Column(nullable=false,length=255)
    private String title;

    @Column(nullable=false,length=255)
    private String author;

    private String message;

    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20)
    private PublicationStatus status;

    @Column(name="created_at",nullable=false)
    private OffsetDateTime createdAt;

    @Column(name="decided_at")
    private OffsetDateTime decidedAt;
}
