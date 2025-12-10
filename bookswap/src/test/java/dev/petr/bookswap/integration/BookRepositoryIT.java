package dev.petr.bookswap.integration;

import dev.petr.bookswap.entity.*;
import dev.petr.bookswap.repository.BookRepository;
import dev.petr.bookswap.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SuppressWarnings("null")
class BookRepositoryIT extends AbstractIntegrationTest {

    @Autowired UserRepository userRepo;
    @Autowired BookRepository bookRepo;

    @Test
    void saveAndLoad() {
        User user = userRepo.save(User.builder()
                .email("u@example.com")
                .passwordHash("hash")
                .displayName("User")
                .role(Role.USER)
                .createdAt(OffsetDateTime.now())
                .build());

        Book book = Book.builder()
                .title("The Pragmatic Programmer")
                .author("Andrew Hunt, David Thomas")
                .isbn("978-0201616224")
                .publishedYear(1999)
                .owner(user)
                .status(BookStatus.AVAILABLE)
                .condition(BookCondition.GOOD)
                .createdAt(OffsetDateTime.now())
                .build();

        book = bookRepo.save(book);
        Long bookId = book.getId();
        assertThat(bookRepo.findById(bookId)).isPresent();
    }
}
