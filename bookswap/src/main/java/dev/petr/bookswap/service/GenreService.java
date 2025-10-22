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

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository repo;

    @Transactional
    public GenreResponse create(GenreCreateRequest req) {
        Genre saved = repo.save(new Genre(null, req.name()));
        return new GenreResponse(saved.getId(), saved.getName());
    }

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
