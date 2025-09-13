package dev.petr.bookswap.repository;

import dev.petr.bookswap.entity.Book;
import dev.petr.bookswap.entity.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    Page<Book> findAllByStatus(BookStatus status, Pageable pageable);

    // "Бесконечная прокрутка" без общего count: берем новые записи после ID
    List<Book> findTop50ByIdLessThanOrderByIdDesc(Long afterId);
}
