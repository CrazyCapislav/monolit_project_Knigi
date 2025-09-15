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
class BookApiIT {

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepo;

    Long uid;

    @BeforeEach void init() {
        uid = userRepo.save(User.builder()
                .email("m@l").passwordHash("h").displayName("Mock User")
                .role(Role.USER).createdAt(OffsetDateTime.now()).build()).getId();
    }

    @Test void create_and_page() throws Exception {
        mvc.perform(post("/api/v1/books")
                        .header("X-User-Id", uid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {"title":"Clean Code","author":"R. Martin","condition":"GOOD","genreIds":[]}
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Clean Code"));

        mvc.perform(get("/api/v1/books?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"));
    }
}
