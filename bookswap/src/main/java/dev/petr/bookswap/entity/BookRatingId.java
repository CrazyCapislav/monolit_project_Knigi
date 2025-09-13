package dev.petr.bookswap.entity;

import lombok.*;
import java.io.Serializable;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class BookRatingId implements Serializable {
    private Long user;
    private Long book;
}
