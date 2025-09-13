package dev.petr.bookswap.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name="book_rating")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@IdClass(BookRatingId.class)
public class BookRating {
    @Id
    @ManyToOne(optional=false) @JoinColumn(name="user_id")
    private User user;

    @Id
    @ManyToOne(optional=false) @JoinColumn(name="book_id")
    private Book book;

    @Column(nullable=false)
    private Integer rating;

    private String comment;

    @Column(name="rated_at", nullable=false)
    private OffsetDateTime ratedAt;
}
