package dev.petr.bookswap.integration;

import dev.petr.bookswap.entity.*;
import dev.petr.bookswap.repository.UserRepository;
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
class PublicationApiIT {

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepo;
    Long requesterId, publisherId;

    @BeforeEach void init() {
        requesterId = userRepo.save(User.builder()
                .email("r@l").passwordHash("h").displayName("Req")
                .role(Role.USER).createdAt(OffsetDateTime.now()).build()).getId();
        publisherId = userRepo.save(User.builder()
                .email("p@l").passwordHash("h").displayName("Pub")
                .role(Role.PUBLISHER).createdAt(OffsetDateTime.now()).build()).getId();
    }

    @Test
    void submitPublication() throws Exception {
        mvc.perform(post("/api/v1/publications")
                        .header("X-User-Id", requesterId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "title":"Book",
                              "author":"Anon",
                              "message":"pls",
                              "publisher_id":%d
                            }
                        """.formatted(publisherId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }
}
