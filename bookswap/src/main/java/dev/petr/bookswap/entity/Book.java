package dev.petr.bookswap.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Book {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable=false, length=255)
    private String title;

    @NotBlank
    @Size(max = 255)
    @Column(nullable=false, length=255)
    private String author;

    @Size(max = 32)
    @Column(length=32)
    private String isbn;

    @Min(0)
    @Max(2100)
    @Column(name="published_year")
    private Integer publishedYear;

    @NotNull
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="owner_id")
    private User owner;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private BookStatus status;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private BookCondition condition;

    @NotNull
    @Column(name="created_at", nullable=false)
    private OffsetDateTime createdAt;

    @ManyToMany
    @JoinTable(
            name = "book_genre",
            joinColumns = @JoinColumn(name="book_id"),
            inverseJoinColumns = @JoinColumn(name="genre_id")
    )
    private Set<Genre> genres;
}
