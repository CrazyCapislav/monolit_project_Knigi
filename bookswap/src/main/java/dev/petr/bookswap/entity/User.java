package dev.petr.bookswap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique = true, length = 320)
    private String email;

    @Column(name="password_hash", nullable=false)
    private String passwordHash;

    @Column(name="display_name", nullable=false, length = 120)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length = 20)
    private Role role;

    @Column(name="created_at", nullable=false)
    private OffsetDateTime createdAt;
}
