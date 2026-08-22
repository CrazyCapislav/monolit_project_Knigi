package dev.petr.bookswap.service;

import dev.petr.bookswap.dto.UserCreateRequest;
import dev.petr.bookswap.entity.Role;
import dev.petr.bookswap.entity.User;
import dev.petr.bookswap.exception.NotFoundException;
import dev.petr.bookswap.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository repo;
    @InjectMocks UserService service;

    @Test
    void shouldRegisterUser() {
        when(repo.save(any())).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(42L);
            return u;
        });

        var resp = service.register(new UserCreateRequest(
                "a@b.c", "Demo", "p"));

        assertThat(resp.id()).isEqualTo(42L);
        verify(repo).save(any(User.class));
    }

    @Test
    void shouldFindUserById() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .role(Role.USER)
                .createdAt(OffsetDateTime.now())
                .build();

        when(repo.findById(1L)).thenReturn(Optional.of(user));

        var resp = service.findById(1L);

        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.email()).isEqualTo("test@example.com");
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(repo.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void shouldGetUserEntity() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .displayName("Test User")
                .role(Role.USER)
                .createdAt(OffsetDateTime.now())
                .build();

        when(repo.findById(1L)).thenReturn(Optional.of(user));

        User result = service.getEntity(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }
}
