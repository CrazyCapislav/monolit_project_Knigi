package dev.petr.bookswap.integration;

import dev.petr.bookswap.entity.*;
import dev.petr.bookswap.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExchangeApiIT {

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepo;
    @Autowired BookRepository bookRepo;

    Long ownerId, requesterId, bookId;

    @BeforeEach void setUp() {
        User owner = userRepo.save(User.builder()
                .email("o@l").passwordHash("h").displayName("Owner")
                .role(Role.USER).createdAt(OffsetDateTime.now()).build());
        User req = userRepo.save(User.builder()
                .email("req_unique@l").passwordHash("h").displayName("Req")
                .role(Role.USER).createdAt(OffsetDateTime.now()).build());
        Book b = bookRepo.save(Book.builder()
                .title("B").author("A").owner(owner).status(BookStatus.AVAILABLE)
                .condition(BookCondition.GOOD).createdAt(OffsetDateTime.now()).build());
        ownerId = owner.getId(); requesterId = req.getId(); bookId = b.getId();
    }

    @Test
    void createExchange() throws Exception {
        mvc.perform(post("/api/v1/exchanges")
                        .header("X-User-Id", requesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "book_requested_id": %d
                            }
                        """.formatted(bookId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("WAITING"));
    }
}
