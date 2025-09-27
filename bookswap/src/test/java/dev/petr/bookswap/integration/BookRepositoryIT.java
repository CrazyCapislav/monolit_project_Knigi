package dev.petr.bookswap.integration;

import dev.petr.bookswap.entity.*;
import dev.petr.bookswap.repository.BookRepository;
import dev.petr.bookswap.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryIT {

    @Autowired UserRepository userRepo;
    @Autowired BookRepository bookRepo;

    @Test
    void saveAndLoad() {
        User u = userRepo.save(User.builder()
                .email("u@example.com")
                .passwordHash("hash")
                .displayName("User")
                .role(Role.USER)
                .createdAt(OffsetDateTime.now())
                .build());

        Book b = Book.builder()
                .title("The Pragmatic Programmer")
                .author("Andrew Hunt, David Thomas")
                .isbn("978-0201616224")
                .publishedYear(1999)
                .owner(u)
                .status(BookStatus.AVAILABLE)
                .condition(BookCondition.GOOD)
                .createdAt(OffsetDateTime.now())
                .build();

        b = bookRepo.save(b);
        assertThat(bookRepo.findById(b.getId())).isPresent();
    }
}
