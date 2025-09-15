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
class ExchangeRepositoryIT {

    @Autowired ExchangeRequestRepository repo;
    @Autowired BookRepository bookRepo;
    @Autowired UserRepository userRepo;

    @Test void findByStatus() {
        User u1 = userRepo.save(User.builder()
                .email("a@b").displayName("A").passwordHash("h")
                .role(Role.USER).createdAt(OffsetDateTime.now()).build());
        Book b = bookRepo.save(Book.builder()
                .title("T").author("X").owner(u1).status(BookStatus.AVAILABLE)
                .condition(BookCondition.GOOD).createdAt(OffsetDateTime.now()).build());
        repo.save(ExchangeRequest.builder()
                .requester(u1).owner(u1).bookRequested(b)
                .status(ExchangeStatus.WAITING).createdAt(OffsetDateTime.now()).build());

        Page<ExchangeRequest> p = repo.findAllByStatus(
                ExchangeStatus.WAITING, PageRequest.of(0, 10));

        assertThat(p.getTotalElements()).isGreaterThan(0);
    }
}
