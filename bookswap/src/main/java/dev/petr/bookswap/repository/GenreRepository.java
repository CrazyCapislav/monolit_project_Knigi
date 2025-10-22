package dev.petr.bookswap.repository;
import dev.petr.bookswap.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    List<Genre> findAllByOrderByNameAsc();
}
