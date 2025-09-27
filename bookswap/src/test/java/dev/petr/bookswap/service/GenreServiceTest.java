package dev.petr.bookswap.service;

import dev.petr.bookswap.dto.GenreCreateRequest;
import dev.petr.bookswap.entity.Genre;
import dev.petr.bookswap.repository.GenreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreServiceTest {

    @Mock GenreRepository repo;
    @InjectMocks GenreService service;

    @Test
    void shouldCreateAndReturnGenre() {
        when(repo.save(any())).thenAnswer(i -> {
            Genre g = i.getArgument(0);
            g.setId(7L);
            return g;
        });

        var resp = service.create(new GenreCreateRequest("Fantasy"));

        assertThat(resp.id()).isEqualTo(7L);
        assertThat(resp.name()).isEqualTo("Fantasy");
        verify(repo).save(any(Genre.class));
    }

    @Test
    void shouldListGenresSorted() {
        when(repo.findAllByOrderByNameAsc())
                .thenReturn(List.of(new Genre(2L,"A"), new Genre(3L,"B")));

        var list = service.findAll();

        assertThat(list).extracting("name").containsExactly("A", "B");
    }

    @Test
    void shouldReturnEntitiesSet() {
        when(repo.findAllById(Set.of(1L)))
                .thenReturn(List.of(new Genre(1L,"X")));

        var set = service.getEntities(Set.of(1L));

        assertThat(set).hasSize(1);
    }
}
