package dev.petr.bookswap.service;

import dev.petr.bookswap.dto.UserCreateRequest;
import dev.petr.bookswap.entity.User;
import dev.petr.bookswap.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
}
