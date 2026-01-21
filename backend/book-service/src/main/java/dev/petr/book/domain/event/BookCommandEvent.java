package dev.petr.book.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookCommandEvent {
    private String commandType;
    private Long bookId;
    private Long currentOwnerId;
    private Long newOwnerId;
    private Long ownerId;
    private Long publicationRequestId;
    private String title;
    private String author;
    private String isbn;
    private Integer publishedYear;
    private String condition;
    private String description;
    private Set<Long> genreIds;
    private Long coverImageId;
}
