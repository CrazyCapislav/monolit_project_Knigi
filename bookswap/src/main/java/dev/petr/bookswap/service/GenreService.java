package dev.petr.bookswap.service;

import dev.petr.bookswap.dto.GenreCreateRequest;
import dev.petr.bookswap.dto.GenreResponse;
import dev.petr.bookswap.entity.Genre;
import dev.petr.bookswap.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for managing book genres.
 */
@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository repo;

    /**
     * Create a new genre.
     * 
     * @param req genre creation request with name
     * @return created genre response
     */
    @Transactional
    public GenreResponse create(GenreCreateRequest req) {
        Genre saved = repo.save(new Genre(null, req.name()));
        return new GenreResponse(saved.getId(), saved.getName());
    }

    /**
     * Get all genres sorted by name.
     * 
     * @return list of all genres
     */
    @Transactional(readOnly = true)
    public List<GenreResponse> findAll() {
        return repo.findAllByOrderByNameAsc().stream().map(g -> new GenreResponse(g.getId(), g.getName())).toList();
    }

    @Transactional(readOnly = true)
    public Set<Genre> getEntities(Set<Long> ids) {
        return new HashSet<>(repo.findAllById(ids));
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
