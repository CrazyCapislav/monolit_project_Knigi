package dev.petr.bookswap.repository;

import dev.petr.bookswap.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class PublicationRepositoryIT {

    @Autowired PublicationRequestRepository repo;
    @Autowired UserRepository userRepo;

    @Test void findByStatus() {
        User requester = userRepo.save(User.builder()
                .email("q@w").displayName("Q").passwordHash("h")
                .role(Role.USER).createdAt(OffsetDateTime.now()).build());
        User publ = userRepo.save(User.builder()
                .email("pub@l").displayName("P").passwordHash("h")
                .role(Role.PUBLISHER).createdAt(OffsetDateTime.now()).build());
        repo.save(PublicationRequest.builder()
                .requester(requester).publisher(publ)
                .title("Book").author("Auth").status(PublicationStatus.SUBMITTED)
                .createdAt(OffsetDateTime.now()).build());

        Page<PublicationRequest> p = repo.findAllByStatus(
                PublicationStatus.SUBMITTED, PageRequest.of(0,10));

        assertThat(p.getContent()).hasSize(1);
    }
}
