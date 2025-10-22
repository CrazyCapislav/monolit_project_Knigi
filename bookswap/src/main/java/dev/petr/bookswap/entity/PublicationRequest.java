package dev.petr.bookswap.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PublicationRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional=false) @JoinColumn(name="requester_id")
    private User requester;

    @NotNull
    @ManyToOne(optional=false) @JoinColumn(name="publisher_id")
    private User publisher;                 // user with PUBLISHER role

    @NotBlank
    @Size(max = 255)
    @Column(nullable=false,length=255)
    private String title;

    @NotBlank
    @Size(max = 255)
    @Column(nullable=false,length=255)
    private String author;

    private String message;

    @NotNull
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20)
    private PublicationStatus status;

    @NotNull
    @Column(name="created_at",nullable=false)
    private OffsetDateTime createdAt;

    @Column(name="decided_at")
    private OffsetDateTime decidedAt;
}
