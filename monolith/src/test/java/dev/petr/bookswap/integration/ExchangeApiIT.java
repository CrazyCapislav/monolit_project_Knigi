package dev.petr.bookswap.integration;

import dev.petr.bookswap.entity.*;
import dev.petr.bookswap.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SuppressWarnings("null")
class ExchangeApiIT extends AbstractIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepo;
    @Autowired BookRepository bookRepo;

    Long ownerId, requesterId, bookId;

    @BeforeEach
    void setUp() {
        User owner = userRepo.save(User.builder()
                .email("o@l").passwordHash("h").displayName("Owner")
                .role(Role.USER).createdAt(OffsetDateTime.now()).build());
        User requester = userRepo.save(User.builder()
                .email("req_unique@l").passwordHash("h").displayName("Req")
                .role(Role.USER).createdAt(OffsetDateTime.now()).build());
        Book book = bookRepo.save(Book.builder()
                .title("B").author("A").owner(owner).status(BookStatus.AVAILABLE)
                .condition(BookCondition.GOOD).createdAt(OffsetDateTime.now()).build());
        ownerId = owner.getId();
        requesterId = requester.getId();
        bookId = book.getId();
    }

    @Test
    void createExchange() throws Exception {
        String requestBody = String.format("""
                            {
                              "book_requested_id": %d
                            }
                        """, bookId);
        mvc.perform(post("/api/v1/exchanges")
                        .header("X-User-Id", requesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("WAITING"));
    }
}
