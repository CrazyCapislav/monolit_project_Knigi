package dev.petr.book.repository;

import dev.petr.book.entity.Book;
import dev.petr.book.entity.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Page<Book> findAllByStatus(BookStatus status, Pageable pageable);

    @Query(value = """
        SELECT DISTINCT b FROM Book b
        LEFT JOIN FETCH b.genres
        ORDER BY b.createdAt DESC, b.id DESC
        """,
            countQuery = """
        SELECT COUNT(DISTINCT b) FROM Book b
        """)
    Page<Book> findAllWithGenres(Pageable pageable);

    @Query("""
        SELECT DISTINCT b FROM Book b
        LEFT JOIN FETCH b.genres
        WHERE (:afterId IS NULL OR b.id < :afterId)
        ORDER BY b.id DESC
        """)
    List<Book> findTopBooksWithGenres(@Param("afterId") Long afterId, Pageable pageable);

    @Query("""
        SELECT DISTINCT b FROM Book b
        LEFT JOIN FETCH b.genres
        WHERE b.ownerId = :ownerId
        ORDER BY b.createdAt DESC
        """)
    List<Book> findByOwnerIdWithGenres(@Param("ownerId") Long ownerId);

    @Query("""
        SELECT b FROM Book b
        LEFT JOIN FETCH b.genres
        WHERE b.id = :id
        """)
    Optional<Book> findByIdWithGenres(@Param("id") Long id);
}