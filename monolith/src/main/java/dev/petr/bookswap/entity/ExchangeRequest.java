package dev.petr.bookswap.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExchangeRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional=false) @JoinColumn(name="requester_id")
    private User requester;

    @NotNull
    @ManyToOne(optional=false) @JoinColumn(name="owner_id")
    private User owner;

    @NotNull
    @ManyToOne(optional=false) @JoinColumn(name="book_requested")
    private Book bookRequested;

    @ManyToOne @JoinColumn(name="book_offered")
    private Book bookOffered;                     // optional

    @NotNull
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20)
    private ExchangeStatus status;

    @NotNull
    @Column(name="created_at",nullable=false)
    private OffsetDateTime createdAt;

    @Column(name="updated_at")
    private OffsetDateTime updatedAt;
}
