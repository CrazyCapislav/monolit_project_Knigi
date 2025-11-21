package dev.petr.exchange.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "exchange_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "requester_id", nullable = false)
    private Long requesterId;
    
    @NotNull
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    
    @NotNull
    @Column(name = "book_requested", nullable = false)
    private Long bookRequestedId;
    
    @Column(name = "book_offered")
    private Long bookOfferedId;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExchangeStatus status;
    
    @NotNull
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
