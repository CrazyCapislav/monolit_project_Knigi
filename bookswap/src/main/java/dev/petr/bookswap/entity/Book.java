package dev.petr.bookswap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.Set;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Book {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=255)
    private String title;

    @Column(nullable=false, length=255)
    private String author;

    @Column(length=32)
    private String isbn;

    @Column(name="published_year")
    private Integer publishedYear;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="owner_id")
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private BookStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private BookCondition condition;

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
