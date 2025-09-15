package dev.petr.bookswap.service;

import dev.petr.bookswap.dto.UserCreateRequest;
import dev.petr.bookswap.entity.*;
import dev.petr.bookswap.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock UserRepository repo;
    @InjectMocks UserService service;

    UserServiceTest(){ MockitoAnnotations.openMocks(this); }

    @Test void register_ok() {
        when(repo.save(any())).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(42L); return u;
        });

        var resp = service.register(new UserCreateRequest(
                "x@y.z","Demo","p"));

        assertThat(resp.id()).isEqualTo(42L);
        verify(repo).save(any(User.class));
    }
}
