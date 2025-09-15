package dev.petr.bookswap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity @Table(name="exchange_request")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExchangeRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="requester_id")
    private User requester;

    @ManyToOne(optional=false) @JoinColumn(name="owner_id")
    private User owner;

    @ManyToOne(optional=false) @JoinColumn(name="book_requested")
    private Book bookRequested;

    @ManyToOne @JoinColumn(name="book_offered")
    private Book bookOffered;                     // optional

    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20)
    private ExchangeStatus status;

    @Column(name="created_at",nullable=false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at")
    private OffsetDateTime updatedAt;
}
