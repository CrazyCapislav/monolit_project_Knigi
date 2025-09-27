package dev.petr.bookswap.repository;

import dev.petr.bookswap.entity.Book;
import dev.petr.bookswap.entity.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Page<Book> findAllByStatus(BookStatus status, Pageable pageable);

    @Query("""
        select b from Book b
        left join fetch b.genres
        where b.id < :afterId
        order by b.id desc
        """)
    List<Book> findTop50ByIdLessThanFetchGenres(Long afterId, Pageable pageable);
}
