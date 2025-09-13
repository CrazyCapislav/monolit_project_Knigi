package dev.petr.bookswap.repository;
import dev.petr.bookswap.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Long> { }
