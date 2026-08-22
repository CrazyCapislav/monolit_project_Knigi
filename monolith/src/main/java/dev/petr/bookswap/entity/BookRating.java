package dev.petr.bookswap.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@IdClass(BookRatingId.class)
public class BookRating {
    @Id
    @NotNull
    @ManyToOne(optional=false) @JoinColumn(name="user_id")
    private User user;

    @Id
    @NotNull
    @ManyToOne(optional=false) @JoinColumn(name="book_id")
    private Book book;

    @NotNull
    @Min(1)
    @Max(5)
    @Column(nullable=false)
    private Integer rating;

    private String comment;

    @NotNull
    @Column(name="rated_at", nullable=false)
    private OffsetDateTime ratedAt;
}
