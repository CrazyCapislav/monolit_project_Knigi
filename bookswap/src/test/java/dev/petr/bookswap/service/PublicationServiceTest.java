package dev.petr.bookswap.service;

import dev.petr.bookswap.dto.PublicationRequestCreateRequest;
import dev.petr.bookswap.entity.*;
import dev.petr.bookswap.repository.*;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PublicationServiceTest {

    @Mock PublicationRequestRepository repo;
    @Mock UserRepository userRepo;
    @InjectMocks PublicationService service;

    User requester = User.builder().id(1L).build();
    User publisher = User.builder().id(9L).role(Role.PUBLISHER).build();

    PublicationServiceTest() { MockitoAnnotations.openMocks(this); }

    @Test void create_ok() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(requester));
        when(userRepo.findById(9L)).thenReturn(Optional.of(publisher));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        var req = new PublicationRequestCreateRequest(
                "New Book","Anon", "pls publish", 9L);

        var resp = service.create(1L, req);

        assertThat(resp.status()).isEqualTo("SUBMITTED");
    }

    @Test void decide_approve() {
        PublicationRequest pr = PublicationRequest.builder()
                .id(7L).publisher(publisher).requester(requester)
                .title("B").author("A").status(PublicationStatus.SUBMITTED)
                .createdAt(OffsetDateTime.now()).build();
        when(repo.findById(7L)).thenReturn(Optional.of(pr));

        var resp = service.decide(7L, 9L, true);

        assertThat(resp.status()).isEqualTo("APPROVED");
    }
}
