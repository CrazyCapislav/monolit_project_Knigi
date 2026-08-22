package dev.petr.bookswap.integration;

import dev.petr.bookswap.entity.*;
import dev.petr.bookswap.repository.UserRepository;
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
class PublicationApiIT extends AbstractIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepo;
    Long requesterId, publisherId;

    @BeforeEach
    void setUp() {
        User requester = userRepo.save(User.builder()
                .email("r@l").passwordHash("h").displayName("Req")
                .role(Role.USER).createdAt(OffsetDateTime.now()).build());
        User publisher = userRepo.save(User.builder()
                .email("p@l").passwordHash("h").displayName("Pub")
                .role(Role.PUBLISHER).createdAt(OffsetDateTime.now()).build());
        requesterId = requester.getId();
        publisherId = publisher.getId();
    }

    @Test
    void submitPublication() throws Exception {
        String requestBody = String.format("""
                            {
                              "title":"Book",
                              "author":"Anon",
                              "message":"pls",
                              "publisher_id":%d
                            }
                        """, publisherId);
        mvc.perform(post("/api/v1/publications")
                        .header("X-User-Id", requesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }
}
