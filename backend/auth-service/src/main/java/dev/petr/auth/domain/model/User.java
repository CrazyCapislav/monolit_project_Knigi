package dev.petr.auth.domain.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("users")
public class User {
    
    @Id
    private Long id;
    
    @Column("email")
    private String email;
    
    @Column("password_hash")
    private String passwordHash;
    
    @Column("display_name")
    private String displayName;
    
    @Column("role")
    private Role role;
    
    @Column("created_at")
    private OffsetDateTime createdAt;
}

