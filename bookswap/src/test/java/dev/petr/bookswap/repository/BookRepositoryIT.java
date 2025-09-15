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
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryIT {

    @Autowired BookRepository bookRepo;
    @Autowired UserRepository userRepo;

    @Test void pageWorks() {
        User u = userRepo.save(User.builder()
                .email("x@y.z").displayName("U").passwordHash("h")
                .role(Role.USER).createdAt(OffsetDateTime.now()).build());
        bookRepo.save(Book.builder()
                .title("B").author("A").owner(u).status(BookStatus.AVAILABLE)
                .condition(BookCondition.GOOD).createdAt(OffsetDateTime.now()).build());

        Page<Book> p = bookRepo.findAll(PageRequest.of(0, 10));
        assertThat(p.getTotalElements()).isGreaterThan(0);
    }
}
