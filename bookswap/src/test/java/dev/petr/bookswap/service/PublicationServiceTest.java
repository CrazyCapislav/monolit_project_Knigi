package dev.petr.bookswap.service;

import dev.petr.bookswap.dto.PublicationRequestCreateRequest;
import dev.petr.bookswap.entity.*;
import dev.petr.bookswap.repository.PublicationRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicationServiceTest {

    @Mock PublicationRequestRepository repo;
    @Mock UserService                  userService;
    @InjectMocks PublicationService    service;

    User requester = User.builder().id(1L).role(Role.USER).build();
    User publisher = User.builder().id(9L).role(Role.PUBLISHER).build();

    @Test
    void shouldCreatePublicationRequest() {
        when(userService.getEntity(1L)).thenReturn(requester);
        when(userService.getEntity(9L)).thenReturn(publisher);
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        var req = new PublicationRequestCreateRequest(
                "Book", "Anon", "pls", 9L);

        var resp = service.create(1L, req);

        assertThat(resp.status()).isEqualTo("SUBMITTED");
    }

    @Test
    void shouldApprovePublicationRequest() {
        PublicationRequest pr = PublicationRequest.builder()
                .id(7L).publisher(publisher).requester(requester)
                .title("B").author("A")
                .status(PublicationStatus.SUBMITTED)
                .createdAt(OffsetDateTime.now()).build();

        when(repo.findById(7L)).thenReturn(Optional.of(pr));

        var resp = service.decide(7L, 9L, true);

        assertThat(resp.status()).isEqualTo("APPROVED");
    }
}
