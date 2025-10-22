package dev.petr.bookswap.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Email
    @Column(nullable=false, unique = true, length = 320)
    private String email;

    @NotBlank
    @Column(name="password_hash", nullable=false)
    private String passwordHash;

    @NotBlank
    @Size(max = 120)
    @Column(name="display_name", nullable=false, length = 120)
    private String displayName;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length = 20)
    private Role role;

    @NotNull
    @Column(name="created_at", nullable=false)
    private OffsetDateTime createdAt;
}
